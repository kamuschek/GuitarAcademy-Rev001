package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PracticeScreen(viewModel: GuitarViewModel) {
    val context = LocalContext.current

    // Mic States
    val isRecording by viewModel.audioAnalyzer.isRecording.collectAsState()
    val volume by viewModel.audioAnalyzer.rmsVolume.collectAsState()
    val frequency by viewModel.audioAnalyzer.frequencyHz.collectAsState()
    val note by viewModel.audioAnalyzer.nearestNote.collectAsState()
    val detectedChord by viewModel.audioAnalyzer.detectedChord.collectAsState()
    val waveformPoints by viewModel.audioAnalyzer.waveformPoints.collectAsState()

    // Interactive targeting chord
    var targetChord by remember { mutableStateOf("G Major") }
    val practiceChords = listOf("G Major", "C Major", "D Major", "A Major", "E Minor", "F Major")

    // Backing track selections
    var activeBackingTrack by remember { mutableStateOf<String?>(null) }
    val backingTracks = listOf(
        "Acoustic Folk Pad (85 BPM) - key G",
        "Classic Blues Shuffle (120 BPM) - key A",
        "Pop Upbeat Strummer (100 BPM) - key C"
    )

    // Manual simulator toggle (extremely useful when mic permissions are blocked or silent)
    var isSimulatingMode by remember { mutableStateOf(false) }

    // Floating guitar string vibration trigger animation
    val matchSuccess = isRecording && detectedChord.trim().lowercase() == targetChord.trim().lowercase()
    val vibrationScaling by animateFloatAsState(
        targetValue = if (matchSuccess) 1.2f else 1.0f,
        animationSpec = repeatable(
            iterations = if (matchSuccess) 6 else 1,
            animation = tween(durationMillis = 80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StringVibrate"
    )

    // Check mic permission
    val hasMicPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission.value = granted
        if (granted) {
            isSimulatingMode = false
            viewModel.startAnalyzing(useSimulationFallback = false)
        } else {
            // fallback gracefully to simulation
            isSimulatingMode = true
            viewModel.startAnalyzing(useSimulationFallback = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoganyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Practice room Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TUNER & PRACTICE ROOM", fontSize = 11.sp, fontWeight = FontWeight.Black, color = SpruceAmber, letterSpacing = 1.sp)
                Text("Real-Time Chord Detection", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            // Mic activation switcher
            Button(
                onClick = {
                    if (isRecording) {
                        viewModel.stopAnalyzing()
                    } else {
                        if (hasMicPermission.value && !isSimulatingMode) {
                            viewModel.startAnalyzing(useSimulationFallback = false)
                        } else {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) StringOrange else SpruceAmber
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("microphone_toggle_btn")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic toggle",
                    tint = MahoganyDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRecording) "Stop Mic" else "Listen",
                    fontWeight = FontWeight.Bold,
                    color = MahoganyDark,
                    fontSize = 12.sp
                )
            }
        }

        // Prototyping Mode notice / simulator helper toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(FretSlate.copy(0.3f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "No guitar nearby? Use simulation helper:",
                fontSize = 11.sp,
                color = WarmRose.copy(0.7f)
            )
            Switch(
                checked = isSimulatingMode,
                onCheckedChange = { active ->
                    isSimulatingMode = active
                    viewModel.stopAnalyzing()
                    viewModel.startAnalyzing(useSimulationFallback = active)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SpruceAmber,
                    checkedTrackColor = SpruceAmber.copy(0.3f)
                ),
                modifier = Modifier.scale(0.8f)
            )
        }

        // 2. Oscilloscope / Real-Time Soundwave Visualizer Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Drawing real-time PCM waveforms custom paths on canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2

                    if (waveformPoints.isEmpty() || !isRecording) {
                        // Flat line when quiet
                        drawLine(
                            color = FretSlate.copy(0.5f),
                            start = androidx.compose.ui.geometry.Offset(0f, centerY),
                            end = androidx.compose.ui.geometry.Offset(width, centerY),
                            strokeWidth = 2f
                        )
                    } else {
                        val path = Path()
                        val stepX = width / (waveformPoints.size - 1)
                        path.moveTo(0f, centerY)

                        for (i in waveformPoints.indices) {
                            val x = i * stepX
                            val value = waveformPoints[i]
                            val y = centerY + (value * centerY * 0.8f)
                            path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = if (matchSuccess) StringCyan else SpruceAmber,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                // Waveform metadata specs overlays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isRecording) "LIVE OSCILLOSCOPE FEED" else "MIC INPUT STANDBY",
                        fontSize = 9.sp,
                        color = WarmRose.copy(0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    if (isRecording) {
                        Text(
                            text = "Volume: ${(volume * 100).toInt()}%",
                            fontSize = 9.sp,
                            color = if (volume > 0.8f) StringOrange else StringCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Central Tuning Dial Gauge Board & Stats representation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Peg / Dial Area (Left Card)
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = RosewoodDeep),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("PITCH METER", fontSize = 10.sp, color = WarmRose.copy(0.5f), fontWeight = FontWeight.Bold)

                    // Tuner Peg dial
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(105.dp)
                            .clip(CircleShape)
                            .background(FretSlate.copy(0.4f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isRecording && frequency > 0) {
                                Text(
                                    text = "${frequency.toInt()} Hz",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrassGold
                                )
                                Text(
                                    text = note,
                                    fontSize = 11.sp,
                                    color = StringCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Ready",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WarmRose.copy(0.4f)
                                )
                                Text(
                                    text = "Strum string",
                                    fontSize = 10.sp,
                                    color = WarmRose.copy(0.4f)
                                )
                            }
                        }
                    }

                    Text("Standard Tuning EADGBE", fontSize = 10.sp, color = WarmRose.copy(0.4f))
                }
            }

            // Target assessment results feedback (Right Card)
            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .height(180.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (matchSuccess) SpruceAmber else RosewoodDeep
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TARGET CHORD ASSISTANCE",
                            fontSize = 10.sp,
                            color = if (matchSuccess) MahoganyDark.copy(0.7f) else WarmRose.copy(0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = targetChord,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (matchSuccess) MahoganyDark else Color.White
                        )
                    }

                    // Matching Assessment status box
                    Column {
                        if (matchSuccess) {
                            Text(
                                "MATCH DETECTED! 🎉",
                                color = MahoganyDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                "Accuracy: 95% - Ringing clean!",
                                color = MahoganyDark.copy(0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "Detected Sound:",
                                color = WarmRose.copy(0.5f),
                                fontSize = 12.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (detectedChord != "None" && detectedChord != "Interference") Icons.Default.MusicNote else Icons.Default.Cancel,
                                    contentDescription = "Sound Status",
                                    tint = if (detectedChord != "None") SpruceAmber else WarmRose.copy(0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = detectedChord,
                                    color = if (detectedChord != "None") WoodContrastHighlight(detectedChord) else WarmRose.copy(0.4f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // quick manual simulator button if simulating
                    if (isSimulatingMode && isRecording) {
                        Button(
                            onClick = { viewModel.simulatedStrum(targetChord) },
                            colors = ButtonDefaults.buttonColors(containerColor = MahoganyDark.copy(0.2f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.align(Alignment.End).testTag("sim_strum_btn")
                        ) {
                            Text("Mock Strum", color = if (matchSuccess) MahoganyDark else Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 4. Target Chord Practicing Selector bar
        Column {
            Text("TAP CHORD TO ANALYZE PRACTICE:", fontSize = 11.sp, color = WarmRose.copy(0.5f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                practiceChords.take(4).forEach { chord ->
                    val isTarget = chord == targetChord
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isTarget) SpruceAmber else FretSlate.copy(0.4f)
                            )
                            .clickable { targetChord = chord }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chord.substringBefore(" "),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTarget) MahoganyDark else Color.White
                        )
                    }
                }
            }
        }

        // 5. Backing Tracks and Music Play Support Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RosewoodDeep.copy(0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QueueMusic, contentDescription = "Backing Track Icon", tint = SpruceAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "BACKING TRACK SUPPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpruceAmber,
                            letterSpacing = 1.sp
                        )
                    }
                    if (activeBackingTrack != null) {
                        Text(
                            "ACTIVE PLAYING",
                            fontSize = 9.sp,
                            color = StringCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StringCyan.copy(0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (activeBackingTrack == null) {
                    Text(
                        "Stream an acoustic loop backing layer to practice leads or keep rhythm timing.",
                        fontSize = 11.sp,
                        color = WarmRose.copy(0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Tracks items
                    backingTracks.forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FretSlate.copy(0.15f))
                                .clickable { activeBackingTrack = track }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(track, fontSize = 12.sp, color = Color.White)
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play track", tint = BrassGold, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    // Backing Track Active screen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FretSlate.copy(0.3f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Volume icon", tint = StringCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    activeBackingTrack ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("Streaming loop enabled - practicing rhythm timing", fontSize = 10.sp, color = WarmRose.copy(0.5f))
                            }
                        }

                        IconButton(onClick = { activeBackingTrack = null }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop backing track", tint = StringOrange)
                        }
                    }
                }
            }
        }
    }
}

private fun WoodContrastHighlight(chord: String): Color {
    return when (chord) {
        "Interference" -> StringOrange
        "None" -> WarmRose
        else -> StringCyan
    }
}

// Scale helper for custom layout
private fun Modifier.scale(scale: Float): Modifier = this
