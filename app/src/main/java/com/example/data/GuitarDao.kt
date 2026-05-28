package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GuitarDao {
    // User Progress
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress)

    // Practice Sessions
    @Query("SELECT * FROM practice_sessions ORDER BY date DESC, id DESC")
    fun getAllSessions(): Flow<List<PracticeSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSession)

    // Guitar Lessons
    @Query("SELECT * FROM guitar_lessons ORDER BY id ASC")
    fun getAllLessons(): Flow<List<GuitarLesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<GuitarLesson>)

    @Update
    suspend fun updateLesson(lesson: GuitarLesson)

    // Forum Posts
    @Query("SELECT * FROM forum_posts ORDER BY id DESC")
    fun getAllForumPosts(): Flow<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPost)

    @Update
    suspend fun updateForumPost(post: ForumPost)

    // Leaderboard
    @Query("SELECT * FROM leaderboard ORDER BY xp DESC")
    fun getLeaderboard(): Flow<List<LeaderboardUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(users: List<LeaderboardUser>)
}
