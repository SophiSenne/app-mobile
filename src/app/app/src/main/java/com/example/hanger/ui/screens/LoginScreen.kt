package com.hanger.app.ui.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanger.app.data.model.User
import com.example.hanger.ui.theme.HangerBlack
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerGold
import com.example.hanger.ui.theme.HangerGray
import com.example.hanger.ui.theme.HangerPink
import android.content.res.Configuration
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview

/**
 * Tela de Login - replica fiel de #screen-login do protótipo HTML.
 *
 * @param onLoginSuccess chamado com o usuário autenticado, retornado por POST /auth/login
 * @param onNavigateToRegister chamado ao tocar em "Criar conta"
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state = viewModel.uiState

    LaunchedEffect(state.loggedInUser) {
        state.loggedInUser?.let { onLoginSuccess(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HangerBlack)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HangerBlack)
                .padding(top = 48.dp, bottom = 36.dp, start = 24.dp, end = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(50.dp))
                Text(
                    "HANGER",
                    color = HangerCream,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 7.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "VISTA SUA HISTÓRIA",
                    color = HangerGold,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    EmojiCircle("\uD83D\uDC57", Color(0xFF5C3D52))
                    EmojiCircle("\uD83D\uDC60", HangerPink)
                    EmojiCircle("\uD83E\uDDE5", HangerGold)
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    "Compartilhe seu estilo com o mundo",
                    color = HangerGray,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp
                    )
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp
                    )
                )
                .background(HangerCream)
                .padding(
                    top = 30.dp,
                    bottom = 60.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            // ===== Corpo do formulário =====
            Column(modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    "Entrar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HangerBlack
                )
                Text(
                    "Bem-vindo de volta ✨",
                    fontSize = 13.sp,
                    color = HangerGray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                AuthTextField(
                    value = emailOrUsername,
                    onValueChange = {
                        emailOrUsername = it
                        viewModel.clearError()
                    },
                    label = "E-mail ou usuário",
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(16.dp))

                AuthTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearError()
                    },
                    label = "Senha",
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        "Esqueci minha senha",
                        color = HangerGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                state.errorMessage?.let {
                    AuthErrorBanner(it)
                    Spacer(Modifier.height(12.dp))
                }

                AuthPrimaryButton(
                    text = "ENTRAR",
                    isLoading = state.isLoading,
                    onClick = { viewModel.login(emailOrUsername, password) }
                )

                AuthSwitchRow(
                    question = "Ainda não tem conta?",
                    actionText = "Criar conta",
                    onClick = onNavigateToRegister
                )
            }
        }
    }
}

@Composable
internal fun EmojiCircle(emoji: String, bg: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(bg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 18.sp)
    }
}

@Composable
internal fun SocialButton(
    label: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        // colors = color.HangerBlack,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD)),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(badgeText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(
    name = "Login Screen",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}