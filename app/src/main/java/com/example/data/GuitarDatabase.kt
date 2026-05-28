package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProgress::class,
        PracticeSession::class,
        GuitarLesson::class,
        ForumPost::class,
        LeaderboardUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GuitarDatabase : RoomDatabase() {
    abstract fun guitarDao(): GuitarDao

    companion object {
        @Volatile
        private var INSTANCE: GuitarDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GuitarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GuitarDatabase::class.java,
                    "guitar_academy_database"
                )
                    .addCallback(GuitarDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class GuitarDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.guitarDao())
                }
            }
        }

        suspend fun populateDatabase(dao: GuitarDao) {
            // 1. Prepopulate default User Progress
            dao.insertOrUpdateProgress(UserProgress())

            // 2. Prepopulate a set of starter guitar lessons
            val defaultLessons = listOf(
                GuitarLesson(
                    title = "Essential Guitar Posture & Tuning",
                    difficulty = "Beginner",
                    durationText = "8 min",
                    description = "Learn the proper sitting posture, hand placements, and standard EADGBE tuning. The critical starting blocks for fretboard success.",
                    chordsToLearn = "E, A",
                    category = "Acoustic Foundations",
                    progressPercent = 100,
                    isCompleted = true
                ),
                GuitarLesson(
                    title = "The Power Triad: G, C & D Major",
                    difficulty = "Beginner",
                    durationText = "12 min",
                    description = "Uncover 80% of pop music with just these three crucial open chords. Master the ring of G Major, the stretch of C, and the shape of D.",
                    chordsToLearn = "G, C, D",
                    category = "Acoustic Foundations",
                    progressPercent = 60,
                    isCompleted = false
                ),
                GuitarLesson(
                    title = "Golden Minor Chords & 4/4 Strumming",
                    difficulty = "Beginner",
                    durationText = "15 min",
                    description = "Add emotional weight to your guitar strings. Practice Aminor and Eminor open chord structures while timing standard down-down-up-up strumming sequences.",
                    chordsToLearn = "Am, Em",
                    category = "Rhythm and Strumming",
                    progressPercent = 0,
                    isCompleted = false
                ),
                GuitarLesson(
                    title = "Conquering the Dreaded F Major Barre Chord",
                    difficulty = "Intermediate",
                    durationText = "20 min",
                    description = "Build finger squeeze strength using your index finger as a temporary nut. Step-by-step techniques to prevent single-string buzz.",
                    chordsToLearn = "F, C, G",
                    category = "Rhythm and Strumming",
                    progressPercent = 0,
                    isCompleted = false
                ),
                GuitarLesson(
                    title = "Pentatonic Rock Improvisation & Bending",
                    difficulty = "Advanced",
                    durationText = "18 min",
                    description = "Release your expressive voice over backing tracks. Learn standard minor pentatonic scales, pitch bends, and vibrato controls on acoustic or electric.",
                    chordsToLearn = "A, D, G",
                    category = "Rock Lead Improvisation",
                    progressPercent = 0,
                    isCompleted = false
                )
            )
            dao.insertLessons(defaultLessons)

            // 3. Prepopulate Practice sessions history for visual trends
            val starterSessions = listOf(
                PracticeSession(
                    date = "2026-05-24",
                    durationMinutes = 20,
                    focusArea = "Posture and Basic Plucking",
                    chordsAttempted = "E,A",
                    accuracyScore = 90,
                    comments = "Great ring and balance on all strings."
                ),
                PracticeSession(
                    date = "2026-05-25",
                    durationMinutes = 15,
                    focusArea = "Tuning & Chord Stretch",
                    chordsAttempted = "E,A",
                    accuracyScore = 85,
                    comments = "Fretting ring is sounding clearer."
                ),
                PracticeSession(
                    date = "2026-05-26",
                    durationMinutes = 30,
                    focusArea = "Power Chords G, C, D",
                    chordsAttempted = "G,C,D",
                    accuracyScore = 70,
                    comments = "Transitioning to C is tough! Finger stretching needed."
                ),
                PracticeSession(
                    date = "2026-05-27",
                    durationMinutes = 25,
                    focusArea = "Rhythm 4/4 Strumming",
                    chordsAttempted = "G,C,D",
                    accuracyScore = 78,
                    comments = "Strumming is solid, focus on keeping index finger flat."
                )
            )
            for (session in starterSessions) {
                dao.insertSession(session)
            }

            // 4. Prepopulate Social Leaderboard users
            val board = listOf(
                LeaderboardUser(name = "Fretmaster Leo", xp = 2800, avatarIndex = 0, rank = 1),
                LeaderboardUser(name = "Acoustic_Jess", xp = 2450, avatarIndex = 1, rank = 2),
                LeaderboardUser(name = "Fingertip_Joe", xp = 2100, avatarIndex = 2, rank = 3),
                LeaderboardUser(name = "Fretboards_Guru", xp = 2000, avatarIndex = 3, rank = 4),
                LeaderboardUser(name = "Guitarist_You", xp = 1850, avatarIndex = 4, isCurrentUser = true, rank = 5),
                LeaderboardUser(name = "Strummer_Sarah", xp = 1420, avatarIndex = 5, rank = 6),
                LeaderboardUser(name = "Picking_Dan", xp = 980, avatarIndex = 6, rank = 7)
            )
            dao.insertLeaderboard(board)

            // 5. Prepopulate Forum discussions
            val posts = listOf(
                ForumPost(
                    authorName = "Acoustic_Jess",
                    authorAvatarIndex = 1,
                    title = "Finally clicked! Barre chord breakthrough",
                    content = "Hey academy! I was struggling with the F Major barre chord for weeks layout, with strings always buzzing. Placing my thumb lower behind the guitar neck finally gave my index finger the leverage it needed. Keep trying design mates, your hands will adapt!",
                    likesCount = 24,
                    repliesCount = 8,
                    timeAgo = "1 day ago"
                ),
                ForumPost(
                    authorName = "Tuner_Wizard",
                    authorAvatarIndex = 3,
                    title = "Tip: Wet your fingertips before practicing? NO!",
                    content = "Please don't practice with wet fingers, it softens the skin and speeds up calluses wearing down. Always dry your hands completely before touching steel strings. Good luck with your daily streaks!",
                    likesCount = 15,
                    repliesCount = 3,
                    timeAgo = "18 hours ago"
                ),
                ForumPost(
                    authorName = "Fretmaster Leo",
                    authorAvatarIndex = 0,
                    title = "Cover: Wish You Were Here (Intro)",
                    content = "Recorded this brief guitar cover of Floyd's classic using Fretboard Coach's built-in backing track tool. Tested with standard G, C, D, Am, Em chords! Feedback on strumming tempo is super welcome.",
                    likesCount = 38,
                    repliesCount = 12,
                    timeAgo = "4 hours ago",
                    isCover = true,
                    songCoverName = "Pink Floyd - Wish You Were Here"
                )
            )
            for (post in posts) {
                dao.insertForumPost(post)
            }
        }
    }
}
