package com.example.luminarysolutions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.luminarysolutions.ui.dashboard.CampaignDashboardScreen
import com.example.luminarysolutions.ui.dashboard.CampaignDetailsScreen
import com.example.luminarysolutions.ui.login.LoginScreen
import com.example.luminarysolutions.ui.register.RegisterScreen
import com.example.luminarysolutions.ui.splash.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        //navigation for splash screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
//navigation for login page
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Routes.REGISTER){
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }

            )
        }
        //navigation for signup page
        composable(Routes.REGISTER) {
            RegisterScreen(
                onLoginClick = {
                    navController.navigate(Routes.LOGIN){
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )

        }

        composable(Routes.HOME) {
            CampaignDashboardScreen(
                onCampaignSelected = { campaignId ->
                    navController.navigate(Routes.campaignDetails(campaignId))
                }
            )
        }

        composable(
            route = Routes.CAMPAIGN_DETAILS,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            CampaignDetailsScreen(
                campaignId = campaignId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
