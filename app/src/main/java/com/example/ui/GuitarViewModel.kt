package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiCoachService
import com.example.audio.AudioChordAnalyzer
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    Dashboard,
    Lessons,
    Practice,
    Forum,
    Coach
}

class GuitarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GuitarRepository
    
    // Core data streams observed by the UI
    val userProgress: StateFlow<UserProgress?>
    val allSessions: StateFlow<List<PracticeSession>>
    val allLessons: StateFlow<List<GuitarLesson>>
    val allForumPosts: StateFlow<List<ForumPost>>
    val leaderboard: StateFlow<List<LeaderboardUser>>

    // UI screen state
    val currentScreen = MutableStateFlow(AppScreen.Dashboard)
    val selectedLesson = MutableStateFlow<GuitarLesson?>(null)

    // Audio Analyzer and mic states wrapping AudioChordAnalyzer
    val audioAnalyzer = AudioChordAnalyzer()
    
    // Coach chatting assistant states
    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Hello there, guitarist! 🎸 I am Fretwise AI, your real-time performance coach. Pitch questions at me about barre chord grip, finger calluses, or tap 'Analyze My Skills' to get custom growth insights based on your practice sessions!" to false
        )
    )
    val chatHistory: StateFlow<List<Pair<String, Boolean>>> = _chatHistory.asStateFlow()

    private val _isCoachLoading = MutableStateFlow(false)
    val isCoachLoading: StateFlow<Boolean> = _isCoachLoading.asStateFlow()

    init {
        val database = GuitarDatabase.getDatabase(application, viewModelScope)
        repository = GuitarRepository(database.guitarDao())

        userProgress = repository.userProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allLessons = repository.allLessons.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allForumPosts = repository.allForumPosts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        leaderboard = repository.leaderboard.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // --- State-based Nav Actions ---
    fun navigateTo(screen: AppScreen) {
        // Automatically close listening mode when navigating away from practice
        if (screen != AppScreen.Practice) {
            stopAnalyzing()
        }
        currentScreen.value = screen
    }

    // --- Audio Analyzer Interaction ---
    fun startAnalyzing(useSimulationFallback: Boolean = false) {
        audioAnalyzer.startListening(viewModelScope, useSimulationFallback)
    }

    fun stopAnalyzing() {
        audioAnalyzer.stopListening()
    }

    fun simulatedStrum(chordName: String) {
        audioAnalyzer.mockTriggerChordStrung(chordName)
    }

    // --- Database Manipulation ---

    /**
     * Complete a lesson and earn XP
     */
    fun completeLesson(lesson: GuitarLesson) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLesson(lesson.copy(isCompleted = true, progressPercent = 100))
            
            // Allocate XP
            val current = userProgress.value ?: UserProgress()
            val newXp = current.xpPoints + 150
            val newPoints = current.totalPracticeMinutes + 10
            
            // Recalculate streak simple rule
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            var currentStreak = current.streak
            if (current.lastPracticeDate != todayStr) {
                currentStreak += 1
            }

            // Save refreshed info
            repository.saveProgress(
                current.copy(
                    xpPoints = newXp,
                    totalPracticeMinutes = newPoints,
                    streak = currentStreak,
                    lastPracticeDate = todayStr
                )
            )

            // Update user in leaderboard
            updateLeaderboardCurrentUser(newXp)
        }
    }

    /**
     * Add a completely new practice session log entry (earns dynamic XP too)
     */
    fun logPracticeSession(minutes: Int, focusArea: String, attemptedChords: String, accuracy: Int, comments: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            val session = PracticeSession(
                date = todayStr,
                durationMinutes = minutes,
                focusArea = focusArea,
                chordsAttempted = attemptedChords,
                accuracyScore = accuracy,
                comments = comments
            )
            repository.addSession(session)

            // Award XP points
            val current = userProgress.value ?: UserProgress()
            val gainedXp = minutes * 5 + (accuracy * 0.5).toInt()
            val newXp = current.xpPoints + gainedXp
            
            var currentStreak = current.streak
            if (current.lastPracticeDate != todayStr) {
                currentStreak += 1
            }

            repository.saveProgress(
                current.copy(
                    streak = currentStreak,
                    totalPracticeMinutes = current.totalPracticeMinutes + minutes,
                    lastPracticeDate = todayStr,
                    xpPoints = newXp
                )
            )

            updateLeaderboardCurrentUser(newXp)
        }
    }

    private suspend fun updateLeaderboardCurrentUser(newXp: Int) {
        // Sync custom user score with leaderboard table
        val updatedLeaderboard = leaderboard.value.map { user ->
            if (user.isCurrentUser) {
                user.copy(xp = newXp)
            } else {
                user
            }
        }.sortedByDescending { it.xp }

        // Recalculate ranks
        val reranked = updatedLeaderboard.mapIndexed { idx, user ->
            user.copy(rank = idx + 1)
        }
        repository.insertLeaderboard(reranked)
    }

    /**
     * Publish a new community post inside the app
     */
    fun publishPost(title: String, content: String, hasCover: Boolean, coverName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = ForumPost(
                authorName = "Guitarist_You",
                authorAvatarIndex = 4, // Yellow/Orange
                title = title,
                content = content,
                isCover = hasCover,
                songCoverName = if (hasCover) coverName else null,
                timeAgo = "Just now",
                likesCount = 0,
                repliesCount = 0
            )
            repository.addForumPost(post)

            // Earn modest post XP
            val current = userProgress.value ?: UserProgress()
            val newXp = current.xpPoints + 50
            repository.saveProgress(current.copy(xpPoints = newXp))
            updateLeaderboardCurrentUser(newXp)
        }
    }

    /**
     * Like/Unlike a Forum post
     */
    fun toggleLikeForumPost(post: ForumPost) {
        viewModelScope.launch(Dispatchers.IO) {
            val newLiked = !post.isLikedByMe
            val countDiff = if (newLiked) 1 else -1
            val updated = post.copy(
                isLikedByMe = newLiked,
                likesCount = maxOf(0, post.likesCount + countDiff)
            )
            repository.updateForumPost(updated)
        }
    }

    // --- AI Performance Coach chat logic ---
    fun askCoachingAdvice(query: String) {
        if (query.trim().isEmpty()) return

        // Add user statement to screen
        _chatHistory.value = _chatHistory.value + (query to true)
        _isCoachLoading.value = true

        viewModelScope.launch {
            val historyList = allSessions.value
            val progressObj = userProgress.value
            
            val feedback = GeminiCoachService.getCoachingFeedback(query, progressObj, historyList)
            
            _isCoachLoading.value = false
            _chatHistory.value = _chatHistory.value + (feedback to false)
        }
    }

    fun triggerAutoCompetenceAnalysis() {
        val analysisQuery = "Analyze my guitar competencies based on my overall practice log history and strength list. Explain specifically what are my biggest strengths and detail an exact, actionable action step plan to focus on my needed growth target areas."
        askCoachingAdvice(analysisQuery)
    }

    fun clearChatHistory() {
        _chatHistory.value = listOf(
            "Hello there, guitarist! 🎸 I am Fretwise AI, your real-time performance coach. Ask me anything or tap 'Analyze My Skills' for automated reports." to false
        )
    }
}
