package com.example.luminarysolutions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.luminarysolutions.data.repository.AuthStatus
import com.example.luminarysolutions.ui.AuthViewModel
import com.example.luminarysolutions.ui.auth.UserRole
import com.example.luminarysolutions.ui.ceo.ApprovalsScreen
import com.example.luminarysolutions.ui.ceo.BeneficiariesScreen
import com.example.luminarysolutions.ui.ceo.CommunityScreen
import com.example.luminarysolutions.ui.ceo.Dashboard.CEODashboardScreen
import com.example.luminarysolutions.ui.ceo.Dashboard.Luminary.LuminaryDetailsScreen
import com.example.luminarysolutions.ui.ceo.Dashboard.Lumisphere.LumiSphereDetailsScreen
import com.example.luminarysolutions.ui.ceo.DonorsScreen
import com.example.luminarysolutions.ui.ceo.ExpensesScreen
import com.example.luminarysolutions.ui.ceo.FinanceScreen
import com.example.luminarysolutions.ui.ceo.FreelanceDetailsScreen
import com.example.luminarysolutions.ui.ceo.GrievancesScreen
import com.example.luminarysolutions.ui.ceo.PartnerDetailsScreen
import com.example.luminarysolutions.ui.ceo.PartnerScreen
import com.example.luminarysolutions.ui.ceo.ProjectDetailsScreen
import com.example.luminarysolutions.ui.ceo.ProjectsScreen
import com.example.luminarysolutions.ui.ceo.ReportsScreen
import com.example.luminarysolutions.ui.ceo.TeamManagementScreen
import com.example.luminarysolutions.ui.ceo.VolunteerDetailsScreen
import com.example.luminarysolutions.ui.dashboard.LandingDashboardScreen
import com.example.luminarysolutions.ui.donor.CampaignDetailsScreen
import com.example.luminarysolutions.ui.donor.CampaignsScreen
import com.example.luminarysolutions.ui.donor.DonationHistoryScreen
import com.example.luminarysolutions.ui.donor.DonationTypeScreen
import com.example.luminarysolutions.ui.donor.DonorDashboardScreen
import com.example.luminarysolutions.ui.donor.DonorMainScreen
import com.example.luminarysolutions.ui.donor.DonorSignUpScreen
import com.example.luminarysolutions.ui.donor.ImpactReportsScreen
import com.example.luminarysolutions.ui.donor.PaymentSelectionScreen
import com.example.luminarysolutions.ui.itadmin.AuditLogsScreen
import com.example.luminarysolutions.ui.itadmin.ITAdminDashboardScreen
import com.example.luminarysolutions.ui.itadmin.RoleDetailsScreen
import com.example.luminarysolutions.ui.itadmin.RolesScreen
import com.example.luminarysolutions.ui.itadmin.SystemSettingsScreen
import com.example.luminarysolutions.ui.itadmin.UsersScreen
import com.example.luminarysolutions.ui.login.LoginScreen
import com.example.luminarysolutions.ui.login.LoginViewModel
import com.example.luminarysolutions.ui.team.TeamDashboardScreen
import com.example.luminarysolutions.ui.volunteer.VolunteerMainScreen
import com.example.luminarysolutions.ui.volunteer.VolunteerSignUpScreen


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.PublicDashboard.route,
    ) {

        // Public Landing Dashboard
        composable(Screen.PublicDashboard.route) {
            // Redirect if already logged in
            LaunchedEffect(authStatus) {
                if (authStatus is AuthStatus.Authenticated) {
                    val status = authStatus as AuthStatus.Authenticated
                    val destination = when (status.role) {
                        UserRole.CEO -> Screen.CEODashboard.route
                        UserRole.ADMIN -> Screen.ITAdminDashboard.route
                        UserRole.VOLUNTEER -> Screen.VolunteerDashboard.route
                        UserRole.DONOR -> Screen.DonorDashboard.route
                        UserRole.TEAM -> Screen.TeamDashboard.route
                        else -> null
                    }
                    if (destination != null) {
                        navController.navigate(destination) {
                            popUpTo(Screen.PublicDashboard.route) { inclusive = true }
                        }
                    }
                }
            }

            LandingDashboardScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onLogoutClick = {
                    authViewModel.signOut()
                    navController.navigate(Screen.PublicDashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
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
        composable(
            route = Screen.Login.route,
            arguments = listOf(navArgument("returnTo") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val returnTo = backStackEntry.arguments?.getString("returnTo")
            val loginRole by loginViewModel.role.collectAsStateWithLifecycle()

            LoginScreen(
                onLoginSuccess = {
                    if (returnTo != null) {
                        navController.navigate(returnTo) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        val destination = when (loginRole) {
                            UserRole.CEO -> Screen.CEODashboard.route
                            UserRole.ADMIN -> Screen.ITAdminDashboard.route
                            UserRole.VOLUNTEER -> Screen.VolunteerDashboard.route
                            UserRole.DONOR -> Screen.DonorDashboard.route
                            UserRole.TEAM -> Screen.TeamDashboard.route
                            else -> Screen.PublicDashboard.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                viewModel = loginViewModel
            )
        }

        // 🔹 CEO Dashboard
        composable(Screen.CEODashboard.route) {
            CEODashboardScreen(
                navController = navController,
                role = UserRole.CEO,
                loginViewModel = loginViewModel
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
                viewModel = loginViewModel
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

        // Donor module routes
        composable(Screen.DonorDashboard.route) { 
            if (authStatus is AuthStatus.Authenticated) {
                DonorMainScreen(navController)
            } else {
                DonorDashboardScreen(
                    navController = navController,
                    onLoginClick = { navController.navigate(Screen.Login.route) }
                )
            }
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
            
            if (authStatus is AuthStatus.Authenticated) {
                // Logged in: Go to payment selection
                PaymentSelectionScreen(
                    projectId = projectId,
                    donorId = (authStatus as AuthStatus.Authenticated).uid,
                    onSuccess = {
                        navController.popBackStack(Screen.PublicDashboard.route, inclusive = false)
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Not logged in: Show choice or redirect to login
                DonationTypeScreen(
                    onLogin = { 
                        val returnRoute = Screen.Donation.createRoute(projectId)
                        navController.navigate(Screen.Login.createRoute(returnRoute)) 
                    },
                    onSignUp = { navController.navigate(Screen.DonorSignUp.route) },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.DonorSignUp.route) {
            DonorSignUpScreen(navController)
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
