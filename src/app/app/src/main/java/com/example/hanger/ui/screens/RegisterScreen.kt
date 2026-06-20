package com.hanger.app.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanger.app.data.model.User
import com.example.hanger.ui.theme.HangerBlack
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerGold
import com.example.hanger.ui.theme.HangerGray
import com.example.hanger.ui.theme.HangerPink

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
                    AuthTextField(
                        value = firstName,
                        onValueChange = { firstName = it; viewModel.clearError() },
                        label = "Nome"
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    AuthTextField(
                        value = lastName,
                        onValueChange = { lastName = it; viewModel.clearError() },
                        label = "Sobrenome"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            AuthTextField(
                value = username,
                onValueChange = { username = it; viewModel.clearError() },
                label = "@ usuário"
            )

            Spacer(Modifier.height(16.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = "E-mail",
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(16.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = "Senha",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(Modifier.height(14.dp))

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
                AuthErrorBanner(it)
                Spacer(Modifier.height(12.dp))
            }

            AuthPrimaryButton(
                text = "CRIAR CONTA",
                isLoading = state.isLoading,
                onClick = {
                    viewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        email = email,
                        password = password,
                        acceptedTerms = acceptedTerms
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