package com.hanger.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanger.app.ui.theme.HangerBlack
import com.hanger.app.ui.theme.HangerGray
import com.hanger.app.ui.theme.HangerGrayLight
import com.hanger.app.ui.theme.HangerInputBg
import com.hanger.app.ui.theme.HangerPink

/** Label estilo "auth-label": uppercase, pequeno, com letter-spacing. */
@Composable
fun AuthLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = HangerGray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}

/** Campo de texto estilo "auth-input": fundo bege claro, vira branco com borda rosa no foco. */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = HangerGrayLight) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                        tint = HangerGrayLight
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = HangerInputBg,
            focusedBorderColor = HangerPink,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = HangerPink,
            focusedTextColor = HangerBlack,
            unfocusedTextColor = HangerBlack
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Botão principal preto estilo "auth-btn". */
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HangerBlack,
            contentColor = Color.White,
            disabledContainerColor = HangerBlack.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.height(20.dp)
            )
        } else {
            Text(text, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

/** Divisor "ou continue com". */
@Composable
fun AuthDivider(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(HangerGrayLight)
        )
        Text(
            text,
            color = HangerGrayLight,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(HangerGrayLight)
        )
    }
}

/** Linha "Ainda não tem conta? Criar conta". */
@Composable
fun AuthSwitchRow(question: String, actionText: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row {
            Text(question, color = HangerGray, fontSize = 13.sp)
            Text(
                " $actionText",
                color = HangerPink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onClick)
            )
        }
    }
}

/** Mensagem de erro estilo banner discreto, exibida acima do botão de ação. */
@Composable
fun AuthErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HangerPink.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(message, color = HangerPink, fontSize = 12.sp)
    }
}
