package com.senkurye.courier.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senkurye.courier.data.local.dao.AssignmentDao
import com.senkurye.courier.data.local.dao.CourierDao
import com.senkurye.courier.data.local.dao.OrderDao
import com.senkurye.courier.data.local.entities.AssignmentEntity
import com.senkurye.courier.data.local.entities.CourierEntity
import com.senkurye.courier.data.local.entities.OrderEntity

/**
 * Main Room database for 'com.senkurye.courier' offline-first data layer.
 */
@Database(
    entities = [
        CourierEntity::class,
        OrderEntity::class,
        AssignmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CourierDatabase : RoomDatabase() {

    abstract fun courierDao(): CourierDao
    abstract fun orderDao(): OrderDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        @Volatile
        private var INSTANCE: CourierDatabase? = null

        fun getInstance(context: Context): CourierDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CourierDatabase::class.java,
                    "senkurye_offline_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
