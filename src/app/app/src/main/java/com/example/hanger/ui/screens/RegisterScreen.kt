package com.hanger.app.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hanger.app.data.model.User
import com.hanger.app.data.repository.UploadResult
import com.hanger.app.data.repository.UserRepository
import com.hanger.app.ui.components.AuthSwitchRow
import com.hanger.app.ui.components.ErrorBanner
import com.hanger.app.ui.components.PrimaryButton
import com.hanger.app.ui.components.TextField
import com.example.hanger.ui.theme.HangerBlack
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerGold
import com.example.hanger.ui.theme.HangerGray
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerPlum
import com.hanger.app.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var locationCity by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var avatarUploadError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            pickedImageUri = it
            avatarUploadError = null
            scope.launch {
                isUploadingAvatar = true
                when (val result = userRepository.uploadAvatar(it, context)) {
                    is UploadResult.Success -> avatarUrl = result.url
                    is UploadResult.Error -> avatarUploadError = result.message
                }
                isUploadingAvatar = false
            }
        }
    }

    val state = viewModel.uiState

    LaunchedEffect(state.loggedInUser) {
        state.loggedInUser?.let { onRegisterSuccess(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HangerCream)
            .verticalScroll(rememberScrollState())
    ) {
        // ===== Cabeçalho escuro =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HangerBlack)
                .padding(top = 36.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = HangerCream)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "HANGER",
                        color = HangerCream,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Criar sua conta",
                    color = HangerCream,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Junte-se à comunidade de moda ✨",
                    color = HangerGold,
                    fontSize = 12.sp
                )
            }
        }

        // ===== Formulário =====
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    TextField(
                        value = firstName,
                        onValueChange = { firstName = it; viewModel.clearError() },
                        label = "Nome"
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it; viewModel.clearError() },
                        label = "Sobrenome"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            TextField(
                value = username,
                onValueChange = { username = it; viewModel.clearError() },
                label = "@ usuário"
            )

            Spacer(Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = "E-mail",
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(16.dp))
            TextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = "Senha",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(Modifier.height(24.dp))

            // ── Seção: Personalizar perfil ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x22000000), thickness = 0.5.dp)
                Text(
                    text = "Personalizar perfil",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HangerGray,
                    letterSpacing = 0.8.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x22000000), thickness = 0.5.dp)
            }

            Spacer(Modifier.height(16.dp))

            // Avatar: círculo clicável com preview + badge de câmera
            val avatarInitials = run {
                val f = firstName.take(1)
                val l = lastName.take(1)
                if (f.isNotBlank() || l.isNotBlank()) "$f$l".uppercase()
                else username.take(2).uppercase().ifEmpty { "?" }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(HangerPlum)
                        .clickable(enabled = !isUploadingAvatar) {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Imagem ou iniciais
                    if (pickedImageUri != null) {
                        AsyncImage(
                            model = pickedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = avatarInitials,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = HangerGold
                        )
                    }

                    // Overlay de carregamento
                    if (isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x99000000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        }
                    }

                    // Badge de câmera (canto inferior direito)
                    if (!isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(HangerPink)
                                .border(1.5.dp, HangerCream, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Escolher foto",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = when {
                        isUploadingAvatar -> "Enviando foto..."
                        avatarUploadError != null -> avatarUploadError!!
                        pickedImageUri != null -> "Toque para trocar a foto"
                        else -> "Toque para adicionar foto de perfil"
                    },
                    fontSize = 12.sp,
                    color = if (avatarUploadError != null) HangerPink else HangerGray
                )
            }

            Spacer(Modifier.height(16.dp))

            TextField(
                value = bio,
                onValueChange = { bio = it; viewModel.clearError() },
                label = "Bio (opcional)",
                singleLine = false,
                minLines = 3
            )

            Spacer(Modifier.height(16.dp))

            TextField(
                value = locationCity,
                onValueChange = { locationCity = it; viewModel.clearError() },
                label = "Cidade"
            )

            Spacer(Modifier.height(20.dp))

            // Checkbox de termos
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { acceptedTerms = !acceptedTerms; viewModel.clearError() }
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            if (acceptedTerms) HangerPink else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (acceptedTerms) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Concordo com os Termos de Uso e Política de Privacidade do Hanger",
                    fontSize = 11.sp,
                    color = HangerGray,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            state.errorMessage?.let {
                ErrorBanner(it)
                Spacer(Modifier.height(12.dp))
            }

            PrimaryButton(
                text = "CRIAR CONTA",
                isLoading = state.isLoading,
                onClick = {
                    viewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        email = email,
                        password = password,
                        acceptedTerms = acceptedTerms,
                        bio = bio,
                        avatarUrl = avatarUrl,
                        locationCity = locationCity
                    )
                }
            )

            Spacer(Modifier.height(18.dp))

            AuthSwitchRow(
                question = "Já tem conta?",
                actionText = "Entrar",
                onClick = onNavigateToLogin
            )
        }
    }
}

@Preview(
    name = "Login Screen",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            onRegisterSuccess = {},
            onNavigateToLogin = {}
        )
    }
}