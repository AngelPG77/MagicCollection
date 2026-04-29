package com.pga.magiccollection.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pga.magiccollection.data.local.dao.*
import com.pga.magiccollection.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        CollectionEntity::class,
        CardOwnedEntity::class,
        RecentCardEntity::class,
        WantListEntity::class,
        WantListCardEntity::class,
        MasterCardEntity::class,
        CardNameFtsEntity::class,
        CardLanguageEntity::class,
        LanguageIndexStateEntity::class,
        MtgSetEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class MagicDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun collectionDao(): CollectionDao
    abstract fun cardOwnedDao(): CardOwnedDao
    abstract fun recentCardDao(): RecentCardDao
    abstract fun wantListDao(): WantListDao
    abstract fun wantListCardDao(): WantListCardDao
    abstract fun cardSearchIndexDao(): CardSearchIndexDao
    abstract fun cardLanguageDao(): CardLanguageDao
    abstract fun languageIndexStateDao(): LanguageIndexStateDao
    abstract fun mtgSetDao(): MtgSetDao
}
