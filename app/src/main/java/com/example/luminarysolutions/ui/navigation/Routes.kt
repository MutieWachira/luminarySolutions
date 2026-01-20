package com.example.luminarysolutions.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "dashboard"
    const val CAMPAIGN_DETAILS = "campaign_details/{campaignId}"

    fun campaignDetails(campaignId: String) = "campaign_details/$campaignId"
}