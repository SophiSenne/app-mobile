package com.hanger.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hanger.app.ui.screens.LoginScreen
import com.hanger.app.ui.screens.RegisterStep1Screen
import com.hanger.app.ui.screens.RegisterStep2Screen

object AuthRoutes {
    const val LOGIN = "login"
    const val REGISTER_STEP1 = "register_step1"
    const val REGISTER_STEP2 = "register_step2/{firstName}/{lastName}/{username}/{email}/{password}"

    fun registerStep2(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String
    ) = "register_step2/$firstName/$lastName/$username/$email/$password"
}

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
    onAuthenticated: () -> Unit = {}
) {
    val sharedAuthViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {

        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { onAuthenticated() },
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER_STEP1)
                },
                viewModel = sharedAuthViewModel
            )
        }

        composable(AuthRoutes.REGISTER_STEP1) {
            RegisterStep1Screen(
                onNext = { firstName, lastName, username, email, password ->
                    navController.navigate(
                        AuthRoutes.registerStep2(firstName, lastName, username, email, password)
                    )
                },
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = sharedAuthViewModel
            )
        }

        composable(
            route = AuthRoutes.REGISTER_STEP2,
            arguments = listOf(
                navArgument("firstName") { type = NavType.StringType },
                navArgument("lastName") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            RegisterStep2Screen(
                firstName = args.getString("firstName", ""),
                lastName = args.getString("lastName", ""),
                username = args.getString("username", ""),
                email = args.getString("email", ""),
                password = args.getString("password", ""),
                onRegisterSuccess = { onAuthenticated() },
                onBack = { navController.popBackStack() },
                viewModel = sharedAuthViewModel
            )
        }
    }
}
