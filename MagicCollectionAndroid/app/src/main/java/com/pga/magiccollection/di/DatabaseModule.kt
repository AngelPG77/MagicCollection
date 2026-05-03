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

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS card_names_fts")
                database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS card_search_fts USING FTS4(card_id, name, oracle_text, language, notindexed=`language`, tokenize=unicode61)")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop inefficient individual indexes
                database.execSQL("DROP INDEX IF EXISTS index_master_cards_colorMask")
                database.execSQL("DROP INDEX IF EXISTS index_master_cards_rarityRank")
                database.execSQL("DROP INDEX IF EXISTS index_master_cards_cmc")
                
                // Create optimized compound index for complex filtering
                database.execSQL("CREATE INDEX IF NOT EXISTS index_master_cards_optimized_search ON master_cards(setCode, colorMask, rarityRank, cmc)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `collection_cards` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `remoteId` INTEGER, 
                        `collectionLocalId` INTEGER NOT NULL, 
                        `scryfallId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `typeLine` TEXT, 
                        `manaCost` TEXT, 
                        `imageUrl` TEXT, 
                        `quantity` INTEGER NOT NULL, 
                        `foil` INTEGER NOT NULL, 
                        `language` TEXT NOT NULL, 
                        `condition` TEXT NOT NULL, 
                        `synced` INTEGER NOT NULL DEFAULT 0, 
                        `pendingDelete` INTEGER NOT NULL DEFAULT 0, 
                        FOREIGN KEY(`collectionLocalId`) REFERENCES `collections`(`localId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_cards_collectionLocalId` ON `collection_cards` (`collectionLocalId`)")
            }
        }

        return Room.databaseBuilder(
            context,
            MagicDatabase::class.java,
            "magic_collection.db"
        ).addMigrations(MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
            .build()
    }

    @Provides
    fun provideUserDao(database: MagicDatabase): UserDao = database.userDao()

    @Provides
    fun provideCollectionDao(database: MagicDatabase): CollectionDao = database.collectionDao()

    @Provides
    fun provideCollectionCardDao(database: MagicDatabase): CollectionCardDao = database.collectionCardDao()

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
