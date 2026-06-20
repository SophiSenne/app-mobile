package com.hanger.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hanger.app.ui.screens.RegisterScreen
import com.hanger.app.ui.screens.LoginScreen

object AuthRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FEED = "feed" 
}

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
    onAuthenticated: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {

        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { user ->
                    // TODO: persistir usuário/sessão (DataStore, etc.) antes de navegar
                    onAuthenticated()
                },
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER)
                }
            )
        }

        composable(AuthRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { user ->
                    onAuthenticated()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
