package com.example.luminarysolutions.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CEODashboard : Screen("ceo_dashboard")
    object StaffDashboard : Screen("staff_dashboard")
    object VolunteerDashboard : Screen("volunteer_dashboard")
    object DonorDashboard : Screen("donor_dashboard")

    //ceo module
    object Projects : Screen("projects")
    object Finance : Screen("finance")
    object Partners : Screen("partners")
    object Community : Screen("community")
    object ProjectDetails : Screen("project_details")
    object Approvals : Screen("approvals")
    object Expenses : Screen("expenses")
    object Reports : Screen("reports")
    object PartnerDetails : Screen("partner_details")

}
