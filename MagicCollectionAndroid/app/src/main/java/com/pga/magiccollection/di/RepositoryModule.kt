package com.pga.magiccollection.di

import com.pga.magiccollection.data.local.dao.CardOwnedDao
import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.dao.UserDao
import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.remote.api.InventoryApi
import com.pga.magiccollection.data.repository.AuthRepository
import com.pga.magiccollection.data.repository.CardRepository
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.data.repository.CollectionRepository
import com.pga.magiccollection.data.repository.InventoryRepository
import com.pga.magiccollection.data.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // All repositories now use @Inject constructor
    // Hilt provides them automatically.
}
