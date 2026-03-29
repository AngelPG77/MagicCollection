package com.pga.magiccollection.di

import android.content.Context
import androidx.room.Room
import com.pga.magiccollection.data.local.MagicDatabase
import com.pga.magiccollection.data.local.dao.CardOwnedDao
import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.dao.RecentCardDao
import com.pga.magiccollection.data.local.dao.UserDao
import com.pga.magiccollection.data.local.security.PreferenceManager
import com.pga.magiccollection.data.local.security.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MagicDatabase {
        return Room.databaseBuilder(
            context,
            MagicDatabase::class.java,
            "magic_collection.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: MagicDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideCollectionDao(database: MagicDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    fun provideCardOwnedDao(database: MagicDatabase): CardOwnedDao {
        return database.cardOwnedDao()
    }

    @Provides
    fun provideRecentCardDao(database: MagicDatabase): RecentCardDao {
        return database.recentCardDao()
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }
}
