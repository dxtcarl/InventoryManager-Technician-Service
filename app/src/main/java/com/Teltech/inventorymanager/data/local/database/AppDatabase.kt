package com.Teltech.inventorymanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.Teltech.inventorymanager.data.local.dao.AiDao
import com.Teltech.inventorymanager.data.local.dao.ProductDao
import com.Teltech.inventorymanager.data.local.dao.RepairDao
import com.Teltech.inventorymanager.data.local.dao.TransactionDao
import com.Teltech.inventorymanager.data.local.dao.UserDao
import com.Teltech.inventorymanager.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        UserProfileEntity::class,
        CustomerEntity::class,
        RepairJobEntity::class,
        RepairPartEntity::class,
        AiMessageEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun repairDao(): RepairDao
    abstract fun aiDao(): AiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}