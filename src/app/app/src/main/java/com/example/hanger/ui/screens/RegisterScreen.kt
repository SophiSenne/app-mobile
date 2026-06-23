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
import androidx.compose.material3.LinearProgressIndicator
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

// ─────────────────────────────────────────────────────────────
// Passo 1 — Dados da conta
// ─────────────────────────────────────────────────────────────

@Composable
fun RegisterStep1Screen(
    onNext: (firstName: String, lastName: String, username: String, email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state = viewModel.uiState

    fun validate(): String? = when {
        firstName.isBlank() || lastName.isBlank() -> "Preencha nome e sobrenome"
        username.isBlank() -> "Informe um nome de usuário"
        email.isBlank() -> "Informe o e-mail"
        password.length < 8 -> "A senha deve ter no mínimo 8 caracteres"
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HangerCream)
            .verticalScroll(rememberScrollState())
    ) {
        RegisterHeader(
            title = "Criar sua conta",
            subtitle = "Junte-se à comunidade de moda ✨",
            step = 1,
            onBack = onNavigateToLogin
        )

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

            state.errorMessage?.let {
                ErrorBanner(it)
                Spacer(Modifier.height(12.dp))
            }

            PrimaryButton(
                text = "CONTINUAR",
                isLoading = false,
                onClick = {
                    val error = validate()
                    if (error != null) {
                        viewModel.setError(error)
                    } else {
                        viewModel.clearError()
                        onNext(firstName, lastName, username, email, password)
                    }
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

// ─────────────────────────────────────────────────────────────
// Passo 2 — Personalização do perfil
// ─────────────────────────────────────────────────────────────

@Composable
fun RegisterStep2Screen(
    firstName: String,
    lastName: String,
    username: String,
    email: String,
    password: String,
    onRegisterSuccess: (User) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var bio by remember { mutableStateOf("") }
    var locationCity by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
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
        RegisterHeader(
            title = "Personalizar perfil",
            subtitle = "Mostre seu estilo para a comunidade 🌟",
            step = 2,
            onBack = onBack
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

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
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(HangerPlum)
                        .clickable(enabled = !isUploadingAvatar) {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (pickedImageUri != null) {
                        AsyncImage(
                            model = pickedImageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = avatarInitials,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = HangerGold
                        )
                    }

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

                    if (!isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(HangerPink)
                                .border(1.5.dp, HangerCream, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Escolher foto",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

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

            Spacer(Modifier.height(24.dp))

            SectionDivider("Sobre você")

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

            Spacer(Modifier.height(24.dp))

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
                        )
                        .border(1.dp, if (acceptedTerms) HangerPink else HangerGray, RoundedCornerShape(4.dp)),
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

            Spacer(Modifier.height(20.dp))

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
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Componentes compartilhados
// ─────────────────────────────────────────────────────────────

@Composable
private fun RegisterHeader(
    title: String,
    subtitle: String,
    step: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HangerBlack)
            .padding(top = 36.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
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
            Text(title, color = HangerCream, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = HangerGold, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            StepIndicator(current = step, total = 2)
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$current de $total",
            color = HangerGray,
            fontSize = 11.sp
        )
        LinearProgressIndicator(
            progress = { current.toFloat() / total.toFloat() },
            modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .clip(RoundedCornerShape(50)),
            color = HangerGold,
            trackColor = Color(0x33FFFFFF)
        )
    }
}

@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x22000000), thickness = 0.5.dp)
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = HangerGray,
            letterSpacing = 0.8.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x22000000), thickness = 0.5.dp)
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview(name = "Register Step 1", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun RegisterStep1Preview() {
    MaterialTheme {
        RegisterStep1Screen(onNext = { _, _, _, _, _ -> }, onNavigateToLogin = {})
    }
}

@Preview(name = "Register Step 2", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun RegisterStep2Preview() {
    MaterialTheme {
        RegisterStep2Screen(
            firstName = "Ana",
            lastName = "Lima",
            username = "analima",
            email = "ana@example.com",
            password = "senha123",
            onRegisterSuccess = {},
            onBack = {}
        )
    }
}
