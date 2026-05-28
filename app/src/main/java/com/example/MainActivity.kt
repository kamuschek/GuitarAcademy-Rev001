package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost() {
    val viewModel: GuitarViewModel = viewModel()
    
    val currentScreen by viewModel.currentScreen.collectAsState()
    val progress by viewModel.userProgress.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val lessons by viewModel.allLessons.collectAsState()
    val posts by viewModel.allForumPosts.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()

    val chatHistory by viewModel.chatHistory.collectAsState()
    val isCoachLoading by viewModel.isCoachLoading.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = RosewoodDeep,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("app_navigation_bar")
            ) {
                // Home/Dashboard Item
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Dashboard,
                    onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Coach") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoganyDark,
                        selectedTextColor = SpruceAmber,
                        indicatorColor = SpruceAmber,
                        unselectedIconColor = WarmRose.copy(0.6f),
                        unselectedTextColor = WarmRose.copy(0.6f)
                    ),
                    modifier = Modifier.testTag("nav_dashboard_item")
                )

                // Lessons Navigation Item
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Lessons,
                    onClick = { viewModel.navigateTo(AppScreen.Lessons) },
                    icon = { Icon(Icons.Default.School, contentDescription = "Lessons") },
                    label = { Text("Lessons") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoganyDark,
                        selectedTextColor = SpruceAmber,
                        indicatorColor = SpruceAmber,
                        unselectedIconColor = WarmRose.copy(0.6f),
                        unselectedTextColor = WarmRose.copy(0.6f)
                    ),
                    modifier = Modifier.testTag("nav_lessons_item")
                )

                // Tuner & Practice Item
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Practice,
                    onClick = { viewModel.navigateTo(AppScreen.Practice) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Tuner Room") },
                    label = { Text("Practice") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoganyDark,
                        selectedTextColor = SpruceAmber,
                        indicatorColor = SpruceAmber,
                        unselectedIconColor = WarmRose.copy(0.6f),
                        unselectedTextColor = WarmRose.copy(0.6f)
                    ),
                    modifier = Modifier.testTag("nav_practice_item")
                )

                // Forums & Leaderboard Item
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Forum,
                    onClick = { viewModel.navigateTo(AppScreen.Forum) },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Community Hub") },
                    label = { Text("Feeds") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoganyDark,
                        selectedTextColor = SpruceAmber,
                        indicatorColor = SpruceAmber,
                        unselectedIconColor = WarmRose.copy(0.6f),
                        unselectedTextColor = WarmRose.copy(0.6f)
                    ),
                    modifier = Modifier.testTag("nav_forum_item")
                )

                // Fretwise AI Coach Room Item
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Coach,
                    onClick = { viewModel.navigateTo(AppScreen.Coach) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Performance Coach") },
                    label = { Text("AI Coach") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoganyDark,
                        selectedTextColor = SpruceAmber,
                        indicatorColor = SpruceAmber,
                        unselectedIconColor = WarmRose.copy(0.6f),
                        unselectedTextColor = WarmRose.copy(0.6f)
                    ),
                    modifier = Modifier.testTag("nav_coach_item")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                AppScreen.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    progress = progress,
                    sessions = sessions,
                    lessons = lessons
                )
                AppScreen.Lessons -> LessonsScreen(
                    viewModel = viewModel,
                    lessons = lessons
                )
                AppScreen.Practice -> PracticeScreen(
                    viewModel = viewModel
                )
                AppScreen.Forum -> ForumScreen(
                    viewModel = viewModel,
                    posts = posts,
                    leaderboard = leaderboard
                )
                AppScreen.Coach -> CoachScreen(
                    viewModel = viewModel,
                    chatHistory = chatHistory,
                    isLoading = isCoachLoading
                )
            }
        }
    }
}
