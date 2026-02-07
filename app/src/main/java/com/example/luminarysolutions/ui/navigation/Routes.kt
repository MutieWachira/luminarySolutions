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
    object Approvals : Screen("approvals")
    object Expenses : Screen("expenses")
    object Reports : Screen("reports")
    object PartnerDetails : Screen("partner_details/{partnerId}"){
        fun createRoute(partnerId: String) = "partner_details/$partnerId"
    }
    object Beneficiaries : Screen("beneficiaries")
    object Grievances : Screen("grievances")
    object Outcomes : Screen("outcomes")

    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }


}
