package com.example.luminarysolutions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.ceo.ApprovalsScreen
import com.example.luminarysolutions.ui.ceo.CEODashboardScreen
import com.example.luminarysolutions.ui.ceo.CommunityScreen
import com.example.luminarysolutions.ui.ceo.ExpensesScreen
import com.example.luminarysolutions.ui.ceo.FinanceScreen
import com.example.luminarysolutions.ui.ceo.PartnerDetailsScreen
import com.example.luminarysolutions.ui.ceo.PartnersDonorsScreen
import com.example.luminarysolutions.ui.ceo.PartnersScreen
import com.example.luminarysolutions.ui.ceo.ProjectDetailsScreen
import com.example.luminarysolutions.ui.ceo.ProjectsScreen
import com.example.luminarysolutions.ui.ceo.ReportsScreen
import com.example.luminarysolutions.ui.ceo.models.ProjectUi
import com.example.luminarysolutions.ui.login.LoginScreen
import com.example.luminarysolutions.ui.login.LoginViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // 🔹 Login screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    when (viewModel.role) {
                        UserRole.CEO ->
                            navController.navigate(Screen.CEODashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }

                        UserRole.IT_ADMIN -> navController.navigate(Screen.StaffDashboard.route)
                        UserRole.VOLUNTEER -> navController.navigate(Screen.VolunteerDashboard.route)
                        UserRole.DONOR -> navController.navigate(Screen.DonorDashboard.route)

                        else -> navController.navigate(Screen.Login.route)
                    }
                },
                viewModel = viewModel
            )
        }

        // 🔹 CEO Dashboard
        composable(Screen.CEODashboard.route) {
            CEODashboardScreen(
                navController = navController,
                role = UserRole.CEO
            )
        }

        // ✅ CEO MODULE ROUTES (so the buttons work)
        composable(Screen.Projects.route) {
            ProjectsScreen(navController = navController)
        }

        composable(Screen.Finance.route) {
            FinanceScreen(navController = navController)
        }

        composable(Screen.Partners.route) {
            PartnersScreen(navController = navController)
        }

        composable(Screen.Community.route) {
            CommunityScreen(navController = navController)
        }
        composable(Screen.ProjectDetails.route) {
            ProjectDetailsScreen(
                navController = navController,
                project = ProjectUi(
                    "Clean Water Initiative",
                    "Ongoing",
                    120000,
                    0.72f,
                    "2 days ago"
                )
            )
        }
        composable(Screen.Approvals.route) { ApprovalsScreen(navController) }
        composable(Screen.Expenses.route) { ExpensesScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
        composable(Screen.Partners.route) { PartnersDonorsScreen(navController) }
        composable(Screen.PartnerDetails.route) { PartnerDetailsScreen(navController) }



    }
}
