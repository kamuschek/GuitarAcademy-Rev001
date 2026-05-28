package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    viewModel: GuitarViewModel,
    chatHistory: List<Pair<String, Boolean>>,
    isLoading: Boolean
) {
    var rawInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Slide up chat rows when new message enters
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    val hintQuestions = listOf(
        "Finger pain and steel strings? Normal?",
        "How do I prevent F Major barre buzz?",
        "Daily 15-min practice outline?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SpruceAmber.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = SpruceAmber, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Fretwise AI Coach 🤖", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Real-Time Performance Coach", fontSize = 10.sp, color = WarmRose.copy(0.4f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Clear conversation history", tint = StringOrange)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Skill competence tracker banner clicker button
            Card(
                colors = CardDefaults.cardColors(containerColor = SpruceAmber),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { viewModel.triggerAutoCompetenceAnalysis() }
                    .testTag("analyze_competency_btn")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QueryStats, contentDescription = "Analyze Progress", tint = MahoganyDark, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ANALYZE SKILL GROWTH CONTEXT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoganyDark,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Fretwise AI reads your weekly practice logs to highlight strengths & needed growth areas instantly.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoganyDark.copy(0.81f)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Analyze Arrow", tint = MahoganyDark)
                }
            }

            // 2. Chat dialogues pane list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chatHistory) { (message, isUser) ->
                    ChatBubbleRow(message = message, isUser = isUser)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RosewoodDeep)
                                .padding(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = SpruceAmber,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Fretwise is scanning your fret fretboard history...", fontSize = 12.sp, color = SpruceAmber)
                        }
                    }
                }
            }

            // 3. Hints Row Helper tags
            ScrollableTabRow(
                selectedTabIndex = 0,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                hintQuestions.forEach { question ->
                    Box(
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FretSlate.copy(0.3f))
                            .clickable {
                                rawInputText = ""
                                viewModel.askCoachingAdvice(question)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(question, fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // 4. Input field controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    placeholder = { Text("Ask about guitar finger coordination...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coach_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpruceAmber,
                        unfocusedBorderColor = FretSlate
                    ),
                    maxLines = 2
                )

                IconButton(
                    onClick = {
                        if (rawInputText.trim().isNotEmpty()) {
                            viewModel.askCoachingAdvice(rawInputText)
                            rawInputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SpruceAmber)
                        .testTag("coach_chat_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MahoganyDark)
                }
            }
        }
    }
}

@Composable
fun ChatBubbleRow(message: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) SpruceAmber else RosewoodDeep
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "You" else "Fretwise AI Coach",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MahoganyDark.copy(0.6f) else SpruceAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = if (isUser) MahoganyDark else Color.White
                )
            }
        }
    }
}
