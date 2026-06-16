package com.example.hanger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

data class Post(
    val username: String,
    val location: String,
    val time: String,
    val likes: Int,
    val comments: Int,
    val caption: String,
    val tags: List<String>,
    val userInitials: String
)

@Composable
fun PostCard(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White)
    ) {
        // User Info Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A394A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.userInitials,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = post.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${post.location} · ${post.time}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // Post Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Look do dia", color = Color.Gray)
        }

        // Stats and Interaction
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row {
                Text(text = post.likes.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = post.comments.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Caption
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(post.username)
                    }
                    append(" ${post.caption}")
                },
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tags
            Row {
                post.tags.forEach { tag ->
                    Text(
                        text = "#$tag",
                        color = Color(0xFFE91E63),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostCardPreview() {
    PostCard(
        post = Post(
            username = "julia.looks",
            location = "São Paulo",
            time = "agora",
            likes = 284,
            comments = 31,
            caption = "Esse vestido midi com sandália rasteira está perfeito para o calor de São Paulo! ☀️",
            tags = listOf("casual", "vestido", "verão"),
            userInitials = "JL"
        )
    )
}
