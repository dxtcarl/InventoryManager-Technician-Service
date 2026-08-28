package com.Teltech.inventorymanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yourname.inventorymanager.data.local.dao.ProductDao
import com.yourname.inventorymanager.data.local.dao.TransactionDao
import com.yourname.inventorymanager.data.local.entity.Product
import com.yourname.inventorymanager.data.local.entity.Transaction

@Database(
    entities = [Product::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}