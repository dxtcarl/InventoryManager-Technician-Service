package com.Teltech.inventorymanager.di

import android.content.Context
import com.yourname.inventorymanager.data.local.dao.ProductDao
import com.yourname.inventorymanager.data.local.dao.TransactionDao
import com.yourname.inventorymanager.data.local.database.AppDatabase
import com.yourname.inventorymanager.data.repository.InventoryRepositoryImpl
import com.yourname.inventorymanager.domain.repository.InventoryRepository
import com.yourname.inventorymanager.domain.usecase.*
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
    @Singleton
    fun provideInventoryRepository(
        productDao: ProductDao,
        transactionDao: TransactionDao
    ): InventoryRepository = InventoryRepositoryImpl(productDao, transactionDao)

    @Provides
    fun provideAddProductUseCase(repo: InventoryRepository) = AddProductUseCase(repo)

    @Provides
    fun provideUpdateStockUseCase(repo: InventoryRepository) = UpdateStockUseCase(repo)

    @Provides
    fun provideGetDashboardUseCase(repo: InventoryRepository) = GetDashboardDataUseCase(repo)
}