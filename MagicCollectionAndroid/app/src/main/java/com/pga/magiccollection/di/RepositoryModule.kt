package com.pga.magiccollection.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // All repositories now use @Inject constructor
    // Hilt provides them automatically.
}
