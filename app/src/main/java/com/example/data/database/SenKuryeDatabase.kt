package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.dao.*
import com.example.data.database.entities.*

@Database(
    entities = [
        CourierProfileEntity::class,
        OrderEntity::class,
        RouteEntity::class,
        RouteStopEntity::class,
        PaymentChangeEntity::class,
        CashTransactionEntity::class,
        PartnerRestaurantEntity::class,
        SyncQueueEntity::class,
        CourierEfficiencyEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SenKuryeDatabase : RoomDatabase() {
    abstract fun courierDao(): CourierDao
    abstract fun orderDao(): OrderDao
    abstract fun routeDao(): RouteDao
    abstract fun paymentChangeDao(): PaymentChangeDao
    abstract fun cashDao(): CashDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun efficiencyDao(): EfficiencyDao

    companion object {
        @Volatile
        private var INSTANCE: SenKuryeDatabase? = null

        fun getInstance(context: Context): SenKuryeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SenKuryeDatabase::class.java,
                    "sen_kurye_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
