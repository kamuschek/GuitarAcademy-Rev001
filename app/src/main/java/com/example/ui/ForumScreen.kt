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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ForumPost
import com.example.data.LeaderboardUser
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    viewModel: GuitarViewModel,
    posts: List<ForumPost>,
    leaderboard: List<LeaderboardUser>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Forum, 1 = Leaderboard
    var showAddPostDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Hub", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddPostDialog = true },
                    containerColor = SpruceAmber,
                    contentColor = MahoganyDark,
                    modifier = Modifier.testTag("add_post_fab")
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = "New Discussion")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Elegant Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RosewoodDeep.copy(0.4f),
                contentColor = SpruceAmber,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SpruceAmber
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Forum, contentDescription = "Forum Icon")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Discussion Board", fontWeight = FontWeight.Bold)
                    }}
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard Icon")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Leaderboards", fontWeight = FontWeight.Bold)
                    }}
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = selectedTab,
                label = "TabContentTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> ForumTab(posts = posts, onLikeClick = { viewModel.toggleLikeForumPost(it) })
                    1 -> LeaderboardTab(leaderboard = leaderboard)
                }
            }
        }
    }

    if (showAddPostDialog) {
        AddPostDialog(
            onDismiss = { showAddPostDialog = false },
            onConfirm = { title, content, isCover, cover ->
                viewModel.publishPost(title, content, isCover, cover)
                showAddPostDialog = false
            }
        )
    }
}

@Composable
fun ForumTab(posts: List<ForumPost>, onLikeClick: (ForumPost) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts) { post ->
            ForumPostCard(post = post, onLikeClick = onLikeClick)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ForumPostCard(post: ForumPost, onLikeClick: (ForumPost) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RosewoodDeep)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Author and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(getAvatarColor(post.authorAvatarIndex))
                ) {
                    Text(
                        text = post.authorName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MahoganyDark,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.authorName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = post.timeAgo,
                        fontSize = 10.sp,
                        color = WarmRose.copy(0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Post content
            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrassGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.content,
                fontSize = 13.sp,
                color = WarmRose
            )

            // 3. Song cover attachment widget (if applicable)
            if (post.isCover && post.songCoverName != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FretSlate.copy(0.3f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { /* play cover simulation */ },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SpruceAmber)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Cover", tint = MahoganyDark, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                nameNormalize(post.songCoverName),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("Song Cover - recorded audio simulation active", fontSize = 10.sp, color = WarmRose.copy(0.4f))
                        }
                    }

                    Badge(containerColor = SpruceAmber.copy(0.15f), contentColor = SpruceAmber) {
                        Text("Cover audio", modifier = Modifier.padding(3.dp), fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = FretSlate.copy(0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Like / comment social statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick(post) }
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like button",
                        tint = if (post.isLikedByMe) StringOrange else WarmRose.copy(0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${post.likesCount}",
                        fontSize = 12.sp,
                        color = if (post.isLikedByMe) StringOrange else WarmRose.copy(0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments count",
                        tint = WarmRose.copy(0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${post.repliesCount}",
                        fontSize = 12.sp,
                        color = WarmRose.copy(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardTab(leaderboard: List<LeaderboardUser>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpruceAmber.copy(0.12f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Cup Logo", tint = SpruceAmber)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Top-fretting learners compete in weekly XP streaks directly!",
                        fontSize = 11.sp,
                        color = SpruceAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(leaderboard) { user ->
            val isMe = user.isCurrentUser
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isMe) SpruceAmber.copy(0.15f) else RosewoodDeep.copy(0.4f)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank numbers
                Text(
                    text = "${user.rank}",
                    color = when (user.rank) {
                        1 -> BrassGold
                        2 -> WarmRose
                        3 -> RosewoodSecondary
                        else -> Color.White.copy(0.5f)
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.width(28.dp)
                )

                // Avatar bubble character
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMe) SpruceAmber else getAvatarColor(user.avatarIndex)
                        )
                ) {
                    Text(
                        user.name.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = MahoganyDark,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name title
                Text(
                    text = user.name + (if (isMe) " (You)" else ""),
                    fontSize = 14.sp,
                    fontWeight = if (isMe) FontWeight.Black else FontWeight.Bold,
                    color = if (isMe) SpruceAmber else Color.White,
                    modifier = Modifier.weight(1f)
                )

                // XP badge
                Badge(
                    containerColor = if (isMe) SpruceAmber else FretSlate.copy(0.4f),
                    contentColor = if (isMe) MahoganyDark else BrassGold
                ) {
                    Text(
                        "${user.xp} XP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AddPostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isCover by remember { mutableStateOf(false) }
    var coverSongName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Guitar Post 📣") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic/Title") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_post_title")
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your tip or progress note...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isCover,
                        onCheckedChange = { isCover = it },
                        colors = CheckboxDefaults.colors(checkedColor = SpruceAmber)
                    )
                    Text("Attach Song Cover recording", fontSize = 12.sp)
                }

                if (isCover) {
                    OutlinedTextField(
                        value = coverSongName,
                        onValueChange = { coverSongName = it },
                        label = { Text("Song Cover Name / Artist") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && content.isNotEmpty()) {
                        onConfirm(title, content, isCover, if (isCover) coverSongName else null)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpruceAmber)
            ) {
                Text("Post", color = MahoganyDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getAvatarColor(idx: Int): Color {
    val colors = listOf(
        Color(0xFFFF8A80),
        Color(0xFFFFD54F),
        Color(0xFF81C784),
        Color(0xFF4DB6AC),
        Color(0xFF64B5F6),
        Color(0xFFBA68C8),
        Color(0xFFE0E0E0)
    )
    return colors.getOrElse(idx) { colors.last() }
}

fun nameNormalize(valName: String?): String {
    if (valName == null) return ""
    return if (valName.length > 30) valName.take(28) + ".." else valName
}
