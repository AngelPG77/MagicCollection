package com.pga.magiccollection.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pga.magiccollection.data.local.converters.MagicConverters
import com.pga.magiccollection.data.local.dao.CardOwnedDao
import com.pga.magiccollection.data.local.dao.CardOwnedQueryDao
import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.dao.MasterCardDao
import com.pga.magiccollection.data.local.dao.UserDao
import com.pga.magiccollection.data.local.entities.CardOwnedEntity
import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.local.entities.MasterCardEntity
import com.pga.magiccollection.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CollectionEntity::class,
        CardOwnedEntity::class,
        MasterCardEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MagicConverters::class)
abstract class MagicDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun collectionDao(): CollectionDao
    abstract fun masterCardDao(): MasterCardDao
    abstract fun cardOwnedDao(): CardOwnedDao
    abstract fun cardOwnedQueryDao(): CardOwnedQueryDao

}