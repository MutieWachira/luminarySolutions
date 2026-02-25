package com.example.luminarysolutions.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")

    //ceo module
    object CEODashboard : Screen("ceo_dashboard")
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

    //IT_ADMIN
    object ITAdminDashboard : Screen("it_admin_dashboard")
    object Users : Screen("it_users")
    object Roles : Screen("roles")
    object RoleDetails : Screen("role_details/{roleID}"){
        fun createRoute(roleID: String) = "role_details/$roleID"
    }
    object AuditLogs : Screen("it_audit_logs")
    object SystemSettings : Screen("it_system_settings")

//volunteer
    object VolunteerDashboard : Screen("volunteer_dashboard")
    object VolunteerTasks : Screen("volunteer_tasks")
    object VolunteerTaskDetails : Screen("volunteer_task_details/{taskId}") {
        fun createRoute(taskId: String) = "volunteer_task_details/$taskId"
    }
    object VolunteerEvents : Screen("volunteer_events")

    //donor
    object DonorDashboard : Screen("donor_dashboard")

    // Donor module
    object DonorCampaigns : Screen("donor_campaigns")
    object DonorDonations : Screen("donor_donations")
    object DonorReports : Screen("donor_reports")

    object CampaignDetails : Screen("campaign_details/{campaignId}") {
        fun createRoute(campaignId: String) = "campaign_details/$campaignId"
    }
}
