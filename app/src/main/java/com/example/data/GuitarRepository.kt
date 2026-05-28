package com.example.data

import kotlinx.coroutines.flow.Flow

class GuitarRepository(private val dao: GuitarDao) {
    val userProgress: Flow<UserProgress?> = dao.getUserProgress()
    val allSessions: Flow<List<PracticeSession>> = dao.getAllSessions()
    val allLessons: Flow<List<GuitarLesson>> = dao.getAllLessons()
    val allForumPosts: Flow<List<ForumPost>> = dao.getAllForumPosts()
    val leaderboard: Flow<List<LeaderboardUser>> = dao.getLeaderboard()

    suspend fun saveProgress(progress: UserProgress) {
        dao.insertOrUpdateProgress(progress)
    }

    suspend fun addSession(session: PracticeSession) {
        dao.insertSession(session)
    }

    suspend fun updateLesson(lesson: GuitarLesson) {
        dao.updateLesson(lesson)
    }

    suspend fun addForumPost(post: ForumPost) {
        dao.insertForumPost(post)
    }

    suspend fun updateForumPost(post: ForumPost) {
        dao.updateForumPost(post)
    }

    suspend fun insertLeaderboard(users: List<LeaderboardUser>) {
        dao.insertLeaderboard(users)
    }
}
