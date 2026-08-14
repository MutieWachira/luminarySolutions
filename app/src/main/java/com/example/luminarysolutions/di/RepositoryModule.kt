package com.example.luminarysolutions.di

import com.example.luminarysolutions.data.repository.CampaignRepository
import com.example.luminarysolutions.data.repository.CampaignRepositoryImpl
import com.example.luminarysolutions.ui.donor.data.DonorRepository
import com.example.luminarysolutions.ui.donor.data.DonorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDonorRepository(
        donorRepositoryImpl: DonorRepositoryImpl
    ): DonorRepository

    @Binds
    @Singleton
    abstract fun bindCampaignRepository(
        campaignRepositoryImpl: CampaignRepositoryImpl
    ): CampaignRepository
}
