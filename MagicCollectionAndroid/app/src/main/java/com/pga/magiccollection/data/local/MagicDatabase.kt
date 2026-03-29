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
        RecentCardEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MagicDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun collectionDao(): CollectionDao
    abstract fun cardOwnedDao(): CardOwnedDao
    abstract fun recentCardDao(): RecentCardDao
}
