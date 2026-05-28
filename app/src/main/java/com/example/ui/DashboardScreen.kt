package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GuitarLesson
import com.example.data.PracticeSession
import com.example.data.UserProgress
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: GuitarViewModel,
    progress: UserProgress?,
    sessions: List<PracticeSession>,
    lessons: List<GuitarLesson>
) {
    var showAddSessionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Guitar Icon",
                            tint = SpruceAmber,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "FretMaster",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "LEVEL 14 • VIRTUOSO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = SpruceAmber,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Practice) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SpruceAmber.copy(0.2f))
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Tuner Room", tint = SpruceAmber)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSessionDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Log Session") },
                text = { Text("Log Practice") },
                containerColor = SpruceAmber,
                contentColor = MahoganyDark,
                modifier = Modifier.testTag("log_practice_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Streak & Summary Hero Card
            item {
                StreakHeroBlock(progress = progress)
            }

            // 2. Personalized Interactive Practice Calendar
            item {
                PracticeCalendarBlock(sessions = sessions)
            }

            // 3. Analytics Progress Log (Bar chart & Strengths analyzer)
            item {
                ProgressChartBlock(sessions = sessions)
            }

            // 4. Competency Summary Highlights (Strengths and Growth Areas)
            item {
                CompetencyOverviewBlock(progress = progress, onAnalyzeClick = {
                    viewModel.navigateTo(AppScreen.Coach)
                    viewModel.triggerAutoCompetenceAnalysis()
                })
            }

            // 5. Gamified Milestones (Achievements Board)
            item {
                MilestonesBlock(progress = progress, sessions = sessions, lessons = lessons)
            }

            // 6. Next Recommended Lesson Section
            item {
                val recommended = lessons.firstOrNull { !it.isCompleted }
                RecommendedLessonBlock(recommended = recommended, onSelect = {
                    viewModel.selectedLesson.value = recommended
                    viewModel.navigateTo(AppScreen.Lessons)
                })
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddSessionDialog) {
        AddPracticeSessionDialog(
            onDismiss = { showAddSessionDialog = false },
            onConfirm = { mins, focus, acc, chords, comment ->
                viewModel.logPracticeSession(mins, focus, chords, acc, comment)
                showAddSessionDialog = false
            }
        )
    }
}

@Composable
fun StreakHeroBlock(progress: UserProgress?) {
    val xp = progress?.xpPoints ?: 0
    val streak = progress?.streak ?: 0
    val totalMins = progress?.totalPracticeMinutes ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RosewoodDeep)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RosewoodDeep,
                            MahoganyDark.copy(0.4f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "READY FOR PRACTICE?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpruceAmber,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fretboard Explorer",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quick Stats strip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(label = "Level XP", value = "$xp", icon = Icons.Default.Stars)
                        StatItem(label = "Practice Time", value = "${totalMins}m", icon = Icons.Default.Timer)
                    }
                }

                // Streak Fire Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(SpruceAmber, BrassGold, SpruceAmber)
                            )
                        )
                        .padding(3.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(RosewoodDeep)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Fire Streak",
                                tint = SpruceAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "$streak Days",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = BrassGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, fontSize = 10.sp, color = WarmRose.copy(0.6f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun PracticeCalendarBlock(sessions: List<PracticeSession>) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    
    // Map of practice days recorded
    val practiceDays = remember(sessions) {
        sessions.map { it.date }.toSet()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RosewoodDeep.copy(0.6f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RosewoodDeep, FretSlate)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "PRACTICE CONSISTENCY CALENDAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpruceAmber,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Shows the current week (7 days) with checking icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Generate past 7 days starting from today backward
                val daysToShow = (6 downTo 0).map { offset ->
                    val day = Calendar.getInstance()
                    day.add(Calendar.DAY_OF_YEAR, -offset)
                    day
                }

                daysToShow.forEach { dayCal ->
                    val dateKey = sdf.format(dayCal.time)
                    val isPracticed = practiceDays.contains(dateKey)
                    val isToday = sdf.format(Date()) == dateKey
                    val dayOfWeek = dayCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
                    val dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH).toString()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        Text(
                            text = dayOfWeek.take(2).uppercase(),
                            fontSize = 10.sp,
                            color = if (isToday) SpruceAmber else WarmRose.copy(0.5f),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPracticed -> SpruceAmber
                                        isToday -> SpruceAmber.copy(0.15f)
                                        else -> FretSlate.copy(0.3f)
                                    }
                                )
                                .clickable { /* view details or log */ }
                        ) {
                            if (isPracticed) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = MahoganyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = dayOfMonth,
                                    fontSize = 12.sp,
                                    color = if (isToday) SpruceAmber else Color.White.copy(0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💡 Practice 10 minutes a day to protect your fiery guitar streak!",
                fontSize = 11.sp,
                color = WarmRose.copy(0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProgressChartBlock(sessions: List<PracticeSession>) {
    val weeklyHoursData = listOf(15f, 20f, 30f, 25f) // Last 4 sessions mins

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RosewoodDeep.copy(0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PRACTICE METRICS ANALYTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpruceAmber,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Weekly view",
                    fontSize = 12.sp,
                    color = WarmRose.copy(0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clean custom visual bar chart representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val sessionsToShow = sessions.takeLast(5).reversed()
                if (sessionsToShow.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No sessions logged yet. Log your first drill!", color = WarmRose.copy(0.4f), fontSize = 12.sp)
                    }
                } else {
                    sessionsToShow.forEachIndexed { idx, s ->
                        val barHeightFactor = minOf(1.0f, s.durationMinutes.toFloat() / 40f)
                        val accuracyColorFactor = s.accuracyScore.toFloat() / 100f
                        val barBrush = Brush.verticalGradient(
                            listOf(
                                SpruceAmber,
                                SpruceAmber.copy(0.3f)
                            )
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "${s.durationMinutes}m",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .fillMaxHeight(barHeightFactor)
                                    .clip(RoundedCornerShape(t1 = 4.dp, t2 = 4.dp, b1 = 0.dp, b2 = 0.dp))
                                    .background(barBrush)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                s.date.substringAfter("-"), // mm-dd
                                fontSize = 10.sp,
                                color = WarmRose.copy(0.4f),
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompetencyOverviewBlock(progress: UserProgress?, onAnalyzeClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RosewoodDeep.copy(0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "FRETBOARD PROGRESS TRACKER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpruceAmber,
                        letterSpacing = 1.sp
                    )
                    Text("Strengths & Needed Growth Targets", fontSize = 12.sp, color = Color.White)
                }

                Button(
                    onClick = onAnalyzeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SpruceAmber.copy(0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach Logo", modifier = Modifier.size(14.dp), tint = SpruceAmber)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Coach", color = SpruceAmber, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Strengths Display List
            Text("💡 STRENGTH ARRESTS (Clean notes played)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StringCyan)
            Spacer(modifier = Modifier.height(4.dp))
            Divider(color = FretSlate.copy(0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            progress?.strengthsList?.split(", ")?.forEach { strength ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = "Check", tint = StringCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strength, fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Areas of Growth
            Text("⚠️ FOCUS GROW AREAS (High difficulty, fret buzz)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StringOrange)
            Spacer(modifier = Modifier.height(4.dp))
            Divider(color = FretSlate.copy(0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            progress?.weaknessesList?.split(", ")?.forEach { weakness ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Default.ReportProblem, contentDescription = "Alert", tint = StringOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(weakness, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MilestonesBlock(progress: UserProgress?, sessions: List<PracticeSession>, lessons: List<GuitarLesson>) {
    val totalMins = progress?.totalPracticeMinutes ?: 0
    val totalLessonsCompleted = lessons.count { it.isCompleted }

    // Achievements specification
    val achievements = listOf(
        Triple("First Tunings", "Log 1 single practice session in the book", sessions.isNotEmpty()),
        Triple("Fiery Gigger", "Maintain a practice streak of 5+ consecutive days", (progress?.streak ?: 0) >= 5),
        Triple("Steel Warrior", "Exceed 300 minutes total practicing duration", totalMins >= 300),
        Triple("Core Foundations", "Mark at least 2 structured lessons completed", totalLessonsCompleted >= 1),
        Triple("Gold Ears Tuner", "Obtain accuracy score exceeding 88% overall on tuner", sessions.any { it.accuracyScore >= 88 })
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RosewoodDeep.copy(0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "GAMIFIED MILESTONES & REWARDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpruceAmber,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            achievements.forEach { (title, description, completed) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (completed) SpruceAmber else FretSlate.copy(0.4f)
                            )
                    ) {
                        Icon(
                            imageVector = if (completed) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = "Lock icon",
                            tint = if (completed) MahoganyDark else WarmRose.copy(0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completed) Color.White else WarmRose.copy(0.5f)
                        )
                        Text(
                            text = description,
                            fontSize = 11.sp,
                            color = if (completed) WarmRose.copy(0.8f) else WarmRose.copy(0.4f)
                        )
                    }

                    if (completed) {
                        Badge(
                            containerColor = StringCyan.copy(0.2f),
                            contentColor = StringCyan
                        ) {
                            Text("+100 XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedLessonBlock(recommended: GuitarLesson?, onSelect: () -> Unit) {
    if (recommended == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SpruceAmber),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "NEXT RECOMMENDED LESSON",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MahoganyDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = recommended.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MahoganyDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recommended.description,
                    fontSize = 12.sp,
                    color = MahoganyDark.copy(0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(containerColor = MahoganyDark, contentColor = Color.White) {
                            Text(recommended.difficulty, modifier = Modifier.padding(3.dp), fontSize = 10.sp)
                        }
                        Badge(containerColor = MahoganyDark.copy(0.2f), contentColor = MahoganyDark) {
                            Text(recommended.durationText, modifier = Modifier.padding(3.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Start Lesson", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoganyDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MahoganyDark)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPracticeSessionDialog(onDismiss: () -> Unit, onConfirm: (Int, String, Int, String, String) -> Unit) {
    var durationText by remember { mutableStateOf("15") }
    var focusArea by remember { mutableStateOf("G, C Chord Switching") }
    var scaleScoreText by remember { mutableStateOf("80") }
    var chordsList by remember { mutableStateOf("G, C, D") }
    var comments by remember { mutableStateOf("Fingers hurting slightly but transitions are smoother!") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Practice Session 🎸") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_duration_input")
                )
                OutlinedTextField(
                    value = focusArea,
                    onValueChange = { focusArea = it },
                    label = { Text("Focus Area") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = scaleScoreText,
                    onValueChange = { scaleScoreText = it },
                    label = { Text("Self-evaluated Accuracy (0-100)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chordsList,
                    onValueChange = { chordsList = it },
                    label = { Text("Chords Practiced (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Comments / Practice notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = durationText.toIntOrNull() ?: 15
                    val score = scaleScoreText.toIntOrNull() ?: 80
                    onConfirm(mins, focusArea, score, chordsList, comments)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpruceAmber)
            ) {
                Text("Log", color = MahoganyDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Rounded corners helper for bars
private fun RoundedCornerShape(t1: androidx.compose.ui.unit.Dp, t2: androidx.compose.ui.unit.Dp, b1: androidx.compose.ui.unit.Dp, b2: androidx.compose.ui.unit.Dp): RoundedCornerShape {
    return RoundedCornerShape(topStart = t1, topEnd = t2, bottomStart = b1, bottomEnd = b2)
}
