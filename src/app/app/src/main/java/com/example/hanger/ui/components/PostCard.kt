package com.hanger.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hanger.app.data.model.PostDto
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerPlaceholderGradients
import com.example.hanger.ui.theme.HangerTextMuted
import com.example.hanger.ui.theme.HangerTextSecondary

/**
 * Card de post no feed (equivalente ao .pin-card do prototipo).
 * A altura da imagem varia conforme [post.imageAspectRatio] para reproduzir
 * o efeito de masonry do Pinterest.
 */
@Composable
fun PostCard(
    post: PostDto,
    indexForPlaceholder: Int,
    onClick: () -> Unit,
    onSaveClick: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            PostImage(
                imageUrl = post.imageUrl,
                aspectRatio = post.imageAspectRatio ?: 1.2f,
                placeholderIndex = indexForPlaceholder,
                isSaved = post.isSavedByMe,
                onSaveClick = onSaveClick
            )

            Column(modifier = Modifier.padding(10.dp, 8.dp, 10.dp, 10.dp)) {
                Text(
                    text = post.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HangerInk,
                    lineHeight = 16.sp,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AuthorAvatar(
                        name = post.author.displayName ?: post.author.username,
                        avatarUrl = post.author.avatarUrl,
                        size = 18.dp,
                        fontSize = 8.sp
                    )
                    Text(
                        text = post.author.username,
                        fontSize = 10.sp,
                        color = HangerTextSecondary,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .clickable(onClick = onLikeClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = if (post.isLikedByMe) HangerPink else HangerTextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = formatLikesCount(post.likesCount),
                        fontSize = 10.sp,
                        color = HangerTextMuted,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PostImage(
    imageUrl: String?,
    aspectRatio: Float,
    placeholderIndex: Int,
    isSaved: Boolean,
    onSaveClick: () -> Unit
) {
    val (colorStart, colorEnd) = HangerPlaceholderGradients[
        placeholderIndex % HangerPlaceholderGradients.size
    ]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(Brush.linearGradient(listOf(colorStart, colorEnd)))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )

        SaveButton(
            isSaved = isSaved,
            onClick = onSaveClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )
    }
}

@Composable
private fun SaveButton(
    isSaved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSaved) HangerInk else HangerPink)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isSaved) "SALVO" else "SALVAR",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AuthorAvatar(
    name: String,
    avatarUrl: String?,
    size: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(HangerPink),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(name),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

private fun formatLikesCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0).replace(".0M", "M")
        count >= 1_000 -> "%.1fK".format(count / 1_000.0).replace(".0K", "K")
        else -> count.toString()
    }
}