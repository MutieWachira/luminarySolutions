package com.example.luminarysolutions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.ceo.ApprovalsScreen
import com.example.luminarysolutions.ui.ceo.BeneficiariesScreen
import com.example.luminarysolutions.ui.ceo.CEODashboardScreen
import com.example.luminarysolutions.ui.ceo.CommunityScreen
import com.example.luminarysolutions.ui.ceo.ExpensesScreen
import com.example.luminarysolutions.ui.ceo.FinanceScreen
import com.example.luminarysolutions.ui.ceo.GrievancesScreen
import com.example.luminarysolutions.ui.ceo.PartnerDetailsScreen
import com.example.luminarysolutions.ui.ceo.PartnersDonorsScreen
import com.example.luminarysolutions.ui.ceo.ProjectDetailsScreen
import com.example.luminarysolutions.ui.ceo.ProjectsScreen
import com.example.luminarysolutions.ui.ceo.ReportsScreen
import com.example.luminarysolutions.ui.donor.CampaignDetailsScreen
import com.example.luminarysolutions.ui.donor.CampaignsScreen
import com.example.luminarysolutions.ui.donor.DonationHistoryScreen
import com.example.luminarysolutions.ui.donor.DonorDashboardScreen
import com.example.luminarysolutions.ui.donor.ImpactReportsScreen
import com.example.luminarysolutions.ui.login.LoginScreen
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.itadmin.ITAdminDashboardScreen
import com.example.luminarysolutions.ui.itadmin.RoleDetailsScreen
import com.example.luminarysolutions.ui.itadmin.UsersScreen
import com.example.luminarysolutions.ui.itadmin.RolesScreen
import com.example.luminarysolutions.ui.itadmin.AuditLogsScreen
import com.example.luminarysolutions.ui.itadmin.SystemSettingsScreen
import com.example.luminarysolutions.ui.volunteer.VolunteerDashboardScreen
import com.example.luminarysolutions.ui.volunteer.VolunteerEventsScreen
import com.example.luminarysolutions.ui.volunteer.VolunteerTaskDetailsScreen
import com.example.luminarysolutions.ui.volunteer.VolunteerTasksScreen


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Login.route,
    ) {

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    when (viewModel.role) {
                        UserRole.CEO ->
                            navController.navigate(Screen.CEODashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }

                        UserRole.ADMIN -> navController.navigate(Screen.ITAdminDashboard.route)
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
                role = UserRole.CEO,
                viewModel = viewModel
            )
        }

        //  CEO Module Routes
        composable(Screen.Projects.route) { ProjectsScreen(navController) }
        composable(Screen.Finance.route) { FinanceScreen(navController) }
        composable(Screen.Partners.route) { PartnersDonorsScreen(navController) } // only one partners screen
        composable(Screen.Community.route) { CommunityScreen(navController) }

        composable(Screen.Approvals.route) { ApprovalsScreen(navController) }
        composable(Screen.Expenses.route) { ExpensesScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }

        composable(Screen.Beneficiaries.route) { BeneficiariesScreen(navController) }
        composable(Screen.Grievances.route) { GrievancesScreen(navController) }

        // Project Details with argument (per-project)
        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailsScreen(navController = navController, projectId = projectId)
        }

        //  Partner Details with argument
        composable(
            route = Screen.PartnerDetails.route,
            arguments = listOf(navArgument("partnerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId") ?: ""
            PartnerDetailsScreen(navController = navController, partnerId = partnerId)
        }

        //IT_Admin Module Routes
        composable(Screen.ITAdminDashboard.route){
            ITAdminDashboardScreen(
                navController = navController,
                role = UserRole.ADMIN,
                viewModel = viewModel
            )
        }
        composable(Screen.Users.route) {
            UsersScreen (
                navController = navController,
                role = UserRole.ADMIN
            )
        }
        composable(Screen.Roles.route){ RolesScreen(navController) }
        composable(
            route=Screen.RoleDetails.route,
            arguments = listOf(navArgument("roleID"){
                type = NavType.StringType })
            ){
            backStackEntry -> val roleId = backStackEntry.arguments?.getString("roleId")?: ""
            RoleDetailsScreen(navController, roleId)
        }
        composable(Screen.AuditLogs.route){ AuditLogsScreen(navController) }
        composable(Screen.SystemSettings.route){ SystemSettingsScreen(navController) }

        //volunteer module routes
        composable(Screen.VolunteerDashboard.route) { VolunteerDashboardScreen(navController) }
        composable(Screen.VolunteerTasks.route) { VolunteerTasksScreen(navController) }
        composable(
         route = Screen.VolunteerTaskDetails.route,
        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
         ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
          VolunteerTaskDetailsScreen(navController, taskId)
        }
        composable(Screen.VolunteerEvents.route) { VolunteerEventsScreen(navController) }

        // Donor module routes
        composable(Screen.DonorDashboard.route) { DonorDashboardScreen(navController) }
        composable(Screen.DonorCampaigns.route) { CampaignsScreen(navController) }
        composable(Screen.DonorDonations.route) { DonationHistoryScreen(navController) }
        composable(Screen.DonorReports.route) { ImpactReportsScreen(navController) }

        composable(
            route = Screen.CampaignDetails.route,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            CampaignDetailsScreen(navController, campaignId)
        }
    }
}
