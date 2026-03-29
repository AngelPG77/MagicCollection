package com.pga.magiccollection.di

import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.dao.UserDao
import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.repository.AuthRepository
import com.pga.magiccollection.data.repository.CardRepository
import com.pga.magiccollection.data.repository.CollectionRepository
import com.pga.magiccollection.data.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        userDao: UserDao,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepository(authApi, userDao, sessionManager)
    }

    @Provides
    @Singleton
    fun provideCardRepository(cardsApi: CardsApi): CardRepository {
        return CardRepository(cardsApi)
    }

    @Provides
    @Singleton
    fun provideCollectionRepository(
        collectionDao: CollectionDao,
        collectionsApi: CollectionsApi
    ): CollectionRepository {
        return CollectionRepository(collectionDao, collectionsApi)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(
        cardOwnedDao: CardOwnedDao,
        inventoryApi: InventoryApi
    ): InventoryRepository {
        return InventoryRepository(cardOwnedDao, inventoryApi)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(sessionManager: SessionManager): SessionRepository {
        return SessionRepository(sessionManager)
    }
}
