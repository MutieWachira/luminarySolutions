package com.example.luminarysolutions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.ceo.*
import com.example.luminarysolutions.ui.ceo.Dashboard.CEODashboardScreen
import com.example.luminarysolutions.ui.ceo.TeamManagementScreen
import com.example.luminarysolutions.ui.ceo.Dashboard.Luminary.LuminaryDetailsScreen
import com.example.luminarysolutions.ui.ceo.Dashboard.Lumisphere.LumiSphereDetailsScreen
import com.example.luminarysolutions.ui.dashboard.LandingDashboardScreen
import com.example.luminarysolutions.ui.dashboard.LandingDashboardViewModel
import com.example.luminarysolutions.ui.donor.*
import com.example.luminarysolutions.ui.login.LoginScreen
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.itadmin.*
import com.example.luminarysolutions.ui.volunteer.*
import com.example.luminarysolutions.ui.volunteer.VolunteerSignUpScreen
import com.example.luminarysolutions.ui.team.TeamDashboardScreen


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.PublicDashboard.route,
    ) {

        // Public Landing Dashboard
        composable(Screen.PublicDashboard.route) {
            LandingDashboardScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onCampaignClick = { campaignId -> 
                    navController.navigate(Screen.CampaignDetails.createRoute(campaignId))
                },
                onVolunteerClick = {
                    navController.navigate(Screen.VolunteerSignUp.createRoute("general"))
                },
                onDonateClick = { projectId ->
                    navController.navigate(Screen.Donation.createRoute(projectId))
                }
            )
        }

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
                        UserRole.TEAM -> navController.navigate(Screen.TeamDashboard.route)
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
                loginViewModel = viewModel
            )
        }

        composable(Screen.LuminaryDetails.route) {
            LuminaryDetailsScreen(navController)
        }

        composable(Screen.LumiSphereDetails.route) {
            LumiSphereDetailsScreen(navController)
        }

        //  CEO Module Routes
        composable(Screen.Projects.route) { ProjectsScreen(navController) }
        composable(Screen.Finance.route) { FinanceScreen(navController) }
        composable(Screen.Partners.route) { PartnerScreen() }
        composable(Screen.Donors.route) { DonorsScreen(navController) } // New Donors Screen
        composable(Screen.Community.route) { CommunityScreen(navController) }

        composable(Screen.Approvals.route) { ApprovalsScreen(navController) }
        composable(
            route = Screen.Expenses.route,
            arguments = listOf(navArgument("projectId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            ExpensesScreen(navController = navController, projectId = projectId)
        }
        composable(Screen.Reports.route) { ReportsScreen(navController) }

        composable(Screen.TeamManagement.route) { TeamManagementScreen(navController) }

        composable(
            route = Screen.VolunteerDetails.route,
            arguments = listOf(navArgument("volunteerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val volunteerId = backStackEntry.arguments?.getString("volunteerId") ?: ""
            VolunteerDetailsScreen(volunteerId = volunteerId, navController = navController)
        }

        composable(Screen.Beneficiaries.route) { BeneficiariesScreen(navController) }
        composable(Screen.Grievances.route) { GrievancesScreen(navController) }

        // Project Details
        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailsScreen(navController = navController, projectId = projectId)
        }

        composable(
            route = Screen.FreelanceDetails.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            FreelanceDetailsScreen(navController = navController, projectId = projectId)
        }

        //  Partner Details
        composable(
            route = Screen.PartnerDetails.route,
            arguments = listOf(navArgument("partnerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId") ?: ""
            PartnerDetailsScreen(navController = navController, partnerId = partnerId)
        }

        // Donor Details (CEO view of donor)
        composable(
            route = Screen.DonorDetails.route,
            arguments = listOf(navArgument("donorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val donorId = backStackEntry.arguments?.getString("donorId") ?: ""
            // Re-using partner details UI for now as structure is similar
            PartnerDetailsScreen(navController = navController, partnerId = donorId)
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
        composable(Screen.VolunteerDashboard.route) { VolunteerMainScreen(navController) }
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
        composable(Screen.DonorDashboard.route) { 
            DonorDashboardScreen(
                navController = navController,
                onLoginClick = { navController.navigate(Screen.Login.route) }
            ) 
        }
        composable(Screen.DonorCampaigns.route) { CampaignsScreen(navController) }
        composable(Screen.DonorDonations.route) { DonationHistoryScreen(navController) }
        composable(Screen.DonorReports.route) { ImpactReportsScreen(navController) }

        // Team Module
        composable(Screen.TeamDashboard.route) { TeamDashboardScreen(navController) }
        
        composable(
            route = Screen.TeamSettings.route,
            arguments = listOf(navArgument("settingType") { type = NavType.StringType })
        ) { backStackEntry ->
            val settingType = backStackEntry.arguments?.getString("settingType") ?: ""
            // We can pass the profile from a shared ViewModel or fetch it again
            // For simplicity, we'll let TeamSettingsScreen handle its own data if needed, 
            // but here we can pass it if we have a shared VM.
            // Using a simple approach for now.
            val teamVm: com.example.luminarysolutions.ui.team.TeamDashboardViewModel = viewModel()
            val uiState by teamVm.uiState.collectAsState()
            
            com.example.luminarysolutions.ui.team.TeamSettingsScreen(
                navController = navController,
                settingType = settingType,
                profile = uiState.userProfile
            )
        }

        composable(
            route = Screen.CampaignDetails.route,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            CampaignDetailsScreen(navController, campaignId)
        }

        // Volunteer & Donation Flow
        composable(
            route = Screen.VolunteerSignUp.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            VolunteerSignUpScreen(navController, projectId)
        }

        composable(
            route = Screen.Donation.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            DonationTypeScreen(
                onSignUp = { navController.navigate(Screen.Login.route) },
                onContinueGuest = { 
                    // Navigate to payment selection, passing projectId if needed
                    navController.navigate("payment_selection/$projectId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "payment_selection/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PaymentSelectionScreen(
                onSuccess = { 
                    navController.popBackStack(Screen.PublicDashboard.route, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
