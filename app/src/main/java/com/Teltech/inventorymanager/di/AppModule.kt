package com.Teltech.inventorymanager.di

import android.content.Context
import com.Teltech.inventorymanager.BuildConfig
import com.Teltech.inventorymanager.data.local.dao.AiDao
import com.Teltech.inventorymanager.data.local.dao.ProductDao
import com.Teltech.inventorymanager.data.local.dao.RepairDao
import com.Teltech.inventorymanager.data.local.dao.TransactionDao
import com.Teltech.inventorymanager.data.local.dao.UserDao
import com.Teltech.inventorymanager.data.local.database.AppDatabase
import com.Teltech.inventorymanager.data.repository.AiRepositoryImpl
import com.Teltech.inventorymanager.domain.repository.AiRepository
import com.Teltech.inventorymanager.domain.repository.InventoryRepositoryImpl
import com.Teltech.inventorymanager.domain.repository.InventoryRepository
import com.Teltech.inventorymanager.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideRepairDao(db: AppDatabase): RepairDao = db.repairDao()

    @Provides
    fun provideAiDao(db: AppDatabase): AiDao = db.aiDao()

    @Provides
    @Singleton
    fun provideInventoryRepository(
        productDao: ProductDao,
        transactionDao: TransactionDao,
        userDao: UserDao,
        repairDao: RepairDao
    ): InventoryRepository = InventoryRepositoryImpl(productDao, transactionDao, userDao, repairDao)

    @Provides
    fun provideAddProductUseCase(repo: InventoryRepository) = AddProductUseCase(repo)

    @Provides
    fun provideUpdateStockUseCase(repo: InventoryRepository) = UpdateStockUseCase(repo)

    @Provides
    fun provideGetDashboardUseCase(repo: InventoryRepository) = GetDashboardDataUseCase(repo)

    @Provides
    @Singleton
    fun provideAiRepository(aiDao: AiDao): AiRepository = AiRepositoryImpl(
        apiKey = BuildConfig.GEMINI_API_KEY,
        aiDao = aiDao
    )
}