package com.senkurye.courier.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senkurye.courier.data.local.entities.AssignmentEntity
import com.senkurye.courier.data.local.entities.CourierEntity
import com.senkurye.courier.data.local.entities.OrderEntity

/**
 * Main Room database class 'AppDatabase' in 'com.senkurye.courier.data.database'.
 * Provides access to DAOs for Courier, Order, and Assignment entities
 * supporting the offline-first architecture.
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
abstract class AppDatabase : RoomDatabase() {

    abstract fun courierDao(): CourierDao
    abstract fun orderDao(): OrderDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        private const val DATABASE_NAME = "sen_kurye_app_database.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
