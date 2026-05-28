package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val streak: Int = 5,
    val totalPracticeMinutes: Int = 320,
    val lastPracticeDate: String = "2026-05-27",
    val strengthsList: String = "C Major open chord accuracy, Consistent 4/4 strumming, Open string resonance",
    val weaknessesList: String = "F Major barre chord buzz, Speed of G to G7 transitions, Hand relaxation",
    val xpPoints: Int = 1850
)

@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"
    val durationMinutes: Int,
    val focusArea: String, // e.g., "Open Chords", "Fingerstyle", "Strumming Pattern #1"
    val chordsAttempted: String, // Comma-separated: "C,G,Am,Em"
    val accuracyScore: Int, // Percentage out of 100
    val comments: String = ""
)

@Entity(tableName = "guitar_lessons")
data class GuitarLesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val durationText: String, // "12m"
    val description: String,
    val chordsToLearn: String, // Comma-separated: "A,D,E"
    val isCompleted: Boolean = false,
    val category: String, // "Acoustic Foundations", "Rhythm and Strumming", "Rock Lead Improvisation"
    val progressPercent: Int = 0,
    val authorName: String = "Coach Mark"
)

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorAvatarIndex: Int, // Index to emoji or color
    val title: String,
    val content: String,
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val timeAgo: String = "Just now",
    val isCover: Boolean = false,
    val songCoverName: String? = null,
    val isLikedByMe: Boolean = false
)

@Entity(tableName = "leaderboard")
data class LeaderboardUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val xp: Int,
    val avatarIndex: Int,
    val isCurrentUser: Boolean = false,
    val rank: Int = 0
)
