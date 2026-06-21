package com.hanger.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hanger.ui.theme.HangerBeige
import com.example.hanger.ui.theme.HangerBorder
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerTextMuted
import com.example.hanger.ui.theme.HangerTextSecondary
import com.hanger.app.data.model.CommentDto
import com.hanger.app.data.model.PostDto
import com.hanger.app.data.repository.PostsRepository
import com.hanger.app.ui.feed.PostDetailUiState
import com.hanger.app.ui.feed.PostDetailViewModel
import com.hanger.app.ui.feed.components.AuthorAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    userId: String = "",
    onNavigateBack: () -> Unit = {}
) {
    val factory = remember(postId, userId) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PostDetailViewModel(PostsRepository(), postId, userId) as T
        }
    }
    val viewModel: PostDetailViewModel = viewModel(key = postId, factory = factory)
    val state by viewModel.uiState.collectAsState()

    var commentText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = HangerCream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.post?.title ?: "Post",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HangerInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = HangerInk
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HangerCream)
            )
        },
        bottomBar = {
            CommentInputBar(
                value = commentText,
                isSending = state.isSendingComment,
                onValueChange = { commentText = it },
                onSend = {
                    viewModel.sendComment(commentText)
                    commentText = ""
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoadingPost) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HangerPink)
            }
        } else {
            val post = state.post
            if (post == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Post não encontrado", color = HangerTextMuted)
                }
            } else {
                PostDetailContent(
                    post = post,
                    state = state,
                    innerPadding = innerPadding,
                    onToggleLike = viewModel::toggleLike,
                    onToggleSave = viewModel::toggleSave
                )
            }
        }

        state.errorMessage?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(4_000)
                viewModel.dismissError()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = HangerPink,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PostDetailContent(
    post: PostDto,
    state: PostDetailUiState,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        item {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.title,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            PostInfo(post = post, onToggleLike = onToggleLike, onToggleSave = onToggleSave)
        }

        item {
            HorizontalDivider(
                color = HangerBorder.copy(alpha = 0.5f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Comentários",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = HangerInk,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        if (state.isLoadingComments) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = HangerPink, strokeWidth = 2.dp)
                }
            }
        } else if (state.comments.isEmpty()) {
            item {
                Text(
                    text = "Nenhum comentário ainda. Seja o primeiro!",
                    fontSize = 13.sp,
                    color = HangerTextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(state.comments, key = { it.id }) { comment ->
                CommentItem(comment = comment)
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun PostInfo(
    post: PostDto,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AuthorAvatar(
                    name = post.author.displayName ?: post.author.username,
                    avatarUrl = post.author.avatarUrl,
                    size = 32.dp,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = post.author.username,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HangerInk
                    )
                    post.city?.let {
                        Text(text = it, fontSize = 11.sp, color = HangerTextMuted)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (post.isSavedByMe) HangerInk else HangerPink)
                    .clickable(onClick = onToggleSave)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (post.isSavedByMe) "SALVO" else "SALVAR",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = post.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HangerInk,
            lineHeight = 24.sp
        )

        post.caption?.takeIf { it.isNotBlank() }?.let { caption ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = caption, fontSize = 14.sp, color = HangerTextSecondary, lineHeight = 20.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Curtir",
                tint = if (post.isLikedByMe) HangerPink else HangerTextMuted,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onToggleLike)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${post.likesCount} curtidas",
                fontSize = 13.sp,
                color = HangerTextMuted
            )
        }

        val metaItems = listOfNotNull(
            post.categoryName?.let { "Categoria: $it" },
            post.typeName?.let { "Tipo: $it" },
            post.weatherCondition?.let { cond ->
                post.temperature?.let { temp -> "$cond · ${temp.toInt()}°C" } ?: cond
            }
        )
        if (metaItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HangerBeige, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                metaItems.forEach { item ->
                    Text(text = item, fontSize = 11.sp, color = HangerTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CommentItem(comment: CommentDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AuthorAvatar(
            name = comment.username,
            avatarUrl = comment.avatarUrl,
            size = 28.dp,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = comment.username,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HangerInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.content,
                fontSize = 13.sp,
                color = HangerInk,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HangerCream)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Adicionar um comentário...", fontSize = 13.sp, color = HangerTextMuted) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HangerPink,
                unfocusedBorderColor = HangerBorder,
                cursorColor = HangerPink
            ),
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() }),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = HangerInk)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (value.isNotBlank()) HangerPink else HangerBorder)
                .clickable(enabled = value.isNotBlank() && !isSending, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
