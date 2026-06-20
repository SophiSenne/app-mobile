package com.example.hanger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hanger.ui.theme.HangerTheme
import com.hanger.app.ui.auth.AuthViewModel
import com.hanger.app.ui.auth.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangerTheme(darkTheme = true) {
                //LoginScreen()
            }
        }
    }
}
