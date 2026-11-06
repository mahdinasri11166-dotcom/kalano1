package com.example.anbar.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val isDefault: Boolean
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val code: String,
    val barcode: String,
    val unit: String,
    val hasVariants: Boolean,
    val trackBatch: Boolean,
    val description: String,
    val purchasePrice: Double,
    val salePrice: Double,
    val category: String,
    val imagePath: String,
    val minStock: Double,
    val maxStock: Double
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val phone: String,
    val address: String,
    val creditBalance: Double,
    val loyaltyPoints: Int,
    val notes: String,
    val createdAt: Long,
    val creditLimit: Double,
    val loyaltyTier: String
)

@Entity(tableName = "stock")
data class StockEntity(
    @PrimaryKey val id: Long,
    val productId: Long,
    val variantId: Long?,
    val warehouseId: Long,
    val quantity: Double,
    val batchNumber: String,
    val expiryDate: Long,
    val unitCost: Double
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: Long,
    val invoiceNumber: String,
    val customerId: Long?,
    val customerName: String,
    val itemsJson: String,
    val subtotal: Double,
    val discountAmount: Double,
    val discountPercent: Double,
    val vatPercent: Double,
    val vatAmount: Double,
    val total: Double,
    val paidAmount: Double,
    val isCredit: Boolean,
    val paymentMethod: String,
    val note: String,
    val warehouseId: Long,
    val timestamp: Long,
    val status: String,
    val cashPaid: Double,
    val cardPaid: Double,
    val creditPaid: Double,
    val originalSaleId: Long,
    val cashierName: String
)

@Entity(tableName = "kv")
data class KvEntity(
    @PrimaryKey val key: String,
    val value: String
)

/** ذخیره JSON برای ماژول‌های باقی‌مانده (شیفت، هزینه، پیک، …) */
@Entity(tableName = "json_store")
data class JsonStoreEntity(
    @PrimaryKey val key: String,
    val json: String
)
