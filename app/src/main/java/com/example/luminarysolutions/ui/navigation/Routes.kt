package com.example.luminarysolutions.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login?returnTo={returnTo}") {
        fun createRoute(returnTo: String? = null) = if (returnTo != null) "login?returnTo=$returnTo" else "login"
    }
    object PublicDashboard : Screen("public_dashboard")

    //ceo module
    object CEODashboard : Screen("ceo_dashboard")
    object LuminaryDetails : Screen("luminary_details")
    object LumiSphereDetails : Screen("lumisphere_details")
    object Projects : Screen("projects")
    object Finance : Screen("finance")
    object Partners : Screen("partners")
    object Donors : Screen("donors")
    object Community : Screen("community")
    object Approvals : Screen("approvals")
    object Expenses : Screen("expenses?projectId={projectId}") {
        fun createRoute(projectId: String? = null) = if (projectId != null) "expenses?projectId=$projectId" else "expenses"
    }
    object Reports : Screen("reports")
    object PartnerDetails : Screen("partner_details/{partnerId}"){
        fun createRoute(partnerId: String) = "partner_details/$partnerId"
    }
    object DonorDetails : Screen("donor_details/{donorId}"){
        fun createRoute(donorId: String) = "donor_details/$donorId"
    }
    object Beneficiaries : Screen("beneficiaries")
    object Grievances : Screen("grievances")
    object Outcomes : Screen("outcomes")

    // Admin module
    object AdminMain : Screen("admin_main")
    object AdminDashboard : Screen("admin_dashboard")

    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }

    object VolunteerDetails : Screen("volunteer_details/{volunteerId}") {
        fun createRoute(volunteerId: String) = "volunteer_details/$volunteerId"
    }

    object TeamManagement : Screen("team_management")

    object FreelanceDetails : Screen("freelance_details/{projectId}") {
        fun createRoute(projectId: String) = "freelance_details/$projectId"
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
    object VolunteerExplore : Screen("volunteer_explore")
    object VolunteerAchievements : Screen("volunteer_achievements")
    object VolunteerProfile : Screen("volunteer_profile")
    object VolunteerNotifications : Screen("volunteer_notifications")
    object VolunteerTasks : Screen("volunteer_tasks")
    object VolunteerTaskDetails : Screen("volunteer_task_details/{taskId}") {
        fun createRoute(taskId: String) = "volunteer_task_details/$taskId"
    }
    object VolunteerEvents : Screen("volunteer_events")

    //donor
    object DonorDashboard : Screen("donor_dashboard")

    //team
    object TeamDashboard : Screen("team_dashboard")
    object TeamSettings : Screen("team_settings/{settingType}") {
        fun createRoute(settingType: String) = "team_settings/$settingType"
    }

    // Donor module
    object DonorCampaigns : Screen("donor_campaigns")
    object DonorDonations : Screen("donor_donations")
    object DonorReports : Screen("donor_reports")
    object DonorProfile : Screen("donor_profile")
    object DonorAchievements : Screen("donor_achievements")

    object CampaignDetails : Screen("campaign_details/{campaignId}") {
        fun createRoute(campaignId: String) = "campaign_details/$campaignId"
    }

    object VolunteerSignUp : Screen("volunteer_signup/{projectId}") {
        fun createRoute(projectId: String) = "volunteer_signup/$projectId"
    }

    object Donation : Screen("donation/{projectId}") {
        fun createRoute(projectId: String) = "donation/$projectId"
    }

    object DonorSignUp : Screen("donor_signup")

    // Client module
    object ClientDashboard : Screen("client_dashboard")
    object ClientFreelanceServices : Screen("client_freelance_services")
    object ClientServiceDetails : Screen("client_service_details/{serviceId}") {
        fun createRoute(serviceId: String) = "client_service_details/$serviceId"
    }
    object ClientProfile : Screen("client_profile")
    object ClientPersonalDetails : Screen("client_personal_details")
    object ClientServiceEnquiry : Screen("client_service_enquiry/{serviceId}") {
        fun createRoute(serviceId: String) = "client_service_enquiry/$serviceId"
    }
    object ClientServiceAcquisition : Screen("client_service_acquisition/{serviceId}") {
        fun createRoute(serviceId: String) = "client_service_acquisition/$serviceId"
    }
}
