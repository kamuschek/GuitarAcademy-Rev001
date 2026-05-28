package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GuitarLesson
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    viewModel: GuitarViewModel,
    lessons: List<GuitarLesson>
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Acoustic Foundations", "Rhythm and Strumming", "Rock Lead Improvisation")

    val activeLesson by viewModel.selectedLesson.collectAsState()

    val filteredLessons = remember(lessons, selectedCategory) {
        if (selectedCategory == "All") {
            lessons
        } else {
            lessons.filter { it.category == selectedCategory }
        }
    }

    AnimatedContent(
        targetState = activeLesson,
        label = "LessonTransition"
    ) { currentActive ->
        if (currentActive != null) {
            // Interactive Video Play Screen
            VideoPlayerView(
                lesson = currentActive,
                onBack = { viewModel.selectedLesson.value = null },
                onComplete = {
                    viewModel.completeLesson(currentActive)
                    viewModel.selectedLesson.value = null
                },
                onPracticeChords = { chordsStr ->
                    viewModel.selectedLesson.value = null
                    viewModel.navigateTo(AppScreen.Practice)
                    // Trigger simulated tuner room configuration if needed
                }
            )
        } else {
            // Main Lessons Library list
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Interactive Lessons", fontWeight = FontWeight.Black) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Category Chips Bar
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).let { if (it == -1) 0 else it },
                        edgePadding = 16.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) SpruceAmber else FretSlate.copy(0.3f)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    color = if (isSelected) MahoganyDark else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredLessons) { lesson ->
                            LessonRowItem(
                                lesson = lesson,
                                onClick = { viewModel.selectedLesson.value = lesson }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonRowItem(lesson: GuitarLesson, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("lesson_card_${lesson.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (lesson.isCompleted) RosewoodDeep.copy(0.4f) else RosewoodDeep
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Preview play button or checked circle indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (lesson.isCompleted) StringCyan.copy(0.15f) else SpruceAmber.copy(0.15f)
                    )
            ) {
                Icon(
                    imageVector = if (lesson.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                    contentDescription = "Status",
                    tint = if (lesson.isCompleted) StringCyan else SpruceAmber,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lesson.category.uppercase(),
                        fontSize = 10.sp,
                        color = SpruceAmber,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = lesson.durationText,
                        fontSize = 11.sp,
                        color = WarmRose.copy(0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.description,
                    fontSize = 12.sp,
                    color = WarmRose.copy(0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Chord targets badge bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CHORDS:", fontSize = 10.sp, color = WarmRose.copy(0.4f), fontWeight = FontWeight.Bold)
                    lesson.chordsToLearn.split(",").forEach { chord ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(FretSlate.copy(0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(chord.trim(), fontSize = 9.sp, color = BrassGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerView(
    lesson: GuitarLesson,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onPracticeChords: (String) -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentSubIdx by remember { mutableStateOf(0) }

    // Mock video playback subtitles timer
    val subtitiles = listOf(
        "Welcome! Tune your guitar strings to standard E-A-D-G-B-E focus first.",
        "Today we're learning the proper fingertip positioning on the fretboard.",
        "Angle your wrist slightly and press close to the metal wires for clean ring.",
        "Let's practice strumming down-down-up-up in 4/4 timing loop.",
        "Press down the notes firmly - click 'Complete' when your fingers are set!"
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                kotlinx.coroutines.delay(4000)
                currentSubIdx = (currentSubIdx + 1) % subtitiles.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoganyDark)
    ) {
        // Simple Back Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = lesson.category.uppercase(),
                    fontSize = 10.sp,
                    color = SpruceAmber,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lesson.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Beautiful Simulated Video Canvas Player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Radial wood gradient acting as wood-guitar sound hole backdrop representation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                RosewoodDeep,
                                Color.Black
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MusicVideo,
                        contentDescription = "Guitar Player",
                        tint = SpruceAmber.copy(0.2f),
                        modifier = Modifier.size(80.dp)
                    )
                    Text("Playing Interactive Coach Video Lesson", color = WarmRose.copy(0.4f), fontSize = 11.sp)
                }
            }

            // Overlay active subtitle tips
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.7f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subtitiles[currentSubIdx],
                    color = BrassGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Quick Floating Control Overlay
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(0.5f))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
            }
        }

        // Mock Progress Seek bar
        LinearProgressIndicator(
            progress = { if (isPlaying) 0.45f else 0.2f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = SpruceAmber,
            trackColor = FretSlate
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Lesson description and instructions block
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RosewoodDeep),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Goal", tint = SpruceAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LESSON DRILL EXPECTATIONS", fontSize = 11.sp, color = SpruceAmber, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lesson.description,
                        fontSize = 13.sp,
                        color = WarmRose
                    )
                }
            }

            // Chords to prepare visual mapping diagrams
            Text("CHORDS TO PREPARE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmRose.copy(0.5f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                lesson.chordsToLearn.split(",").forEach { chord ->
                    ChordDiagramMiniCard(chordName = chord.trim())
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Option 1: Practice live right now on mic
            OutlinedButton(
                onClick = { onPracticeChords(lesson.chordsToLearn) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpruceAmber),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(SpruceAmber, BrassGold)))
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Mic")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Practice Live on Tuner / Mic", fontWeight = FontWeight.Bold)
            }

            // Option 2: Complete and claim XP
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("submit_lesson_complete"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpruceAmber)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Reward", tint = MahoganyDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete & Claim +150 XP", fontWeight = FontWeight.Bold, color = MahoganyDark)
            }
        }
    }
}

@Composable
fun ChordDiagramMiniCard(chordName: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FretSlate.copy(0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(chordName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = BrassGold)
            Spacer(modifier = Modifier.height(4.dp))
            // Minimalist chord grid visualizer drawing representation
            Box(
                modifier = Modifier
                    .size(40.dp, 50.dp)
                    .background(RosewoodDeep)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                // Mock visual strings lines
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(4) {
                        Divider(color = Color.White.copy(0.15f), thickness = 1.dp)
                    }
                }
                // Mock finger dots representation
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(SpruceAmber)
                )
            }
        }
    }
}
