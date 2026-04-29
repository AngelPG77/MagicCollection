package com.pga.magiccollection.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pga.magiccollection.data.local.MagicDatabase
import com.pga.magiccollection.data.local.dao.*
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
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE collections ADD COLUMN pendingDelete INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE want_lists ADD COLUMN pendingDelete INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE cards_owned ADD COLUMN pendingDelete INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE want_list_cards ADD COLUMN pendingDelete INTEGER NOT NULL DEFAULT 0")
            }
        }

        return Room.databaseBuilder(
            context,
            MagicDatabase::class.java,
            "magic_collection.db"
        ).addMigrations(MIGRATION_12_13)
            .build()
    }

    @Provides
    fun provideUserDao(database: MagicDatabase): UserDao = database.userDao()

    @Provides
    fun provideCollectionDao(database: MagicDatabase): CollectionDao = database.collectionDao()

    @Provides
    fun provideCardOwnedDao(database: MagicDatabase): CardOwnedDao = database.cardOwnedDao()

    @Provides
    fun provideRecentCardDao(database: MagicDatabase): RecentCardDao = database.recentCardDao()

    @Provides
    fun provideWantListDao(database: MagicDatabase): WantListDao = database.wantListDao()

    @Provides
    fun provideWantListCardDao(database: MagicDatabase): WantListCardDao = database.wantListCardDao()

    @Provides
    fun provideCardSearchIndexDao(database: MagicDatabase): CardSearchIndexDao = database.cardSearchIndexDao()

    @Provides
    fun provideCardLanguageDao(database: MagicDatabase): CardLanguageDao = database.cardLanguageDao()

    @Provides
    fun provideLanguageIndexStateDao(database: MagicDatabase): LanguageIndexStateDao = database.languageIndexStateDao()

    @Provides
    fun provideMtgSetDao(database: MagicDatabase): MtgSetDao = database.mtgSetDao()

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager = SessionManager(context)

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager = PreferenceManager(context)
}
