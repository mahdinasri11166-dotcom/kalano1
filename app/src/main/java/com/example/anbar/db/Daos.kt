package com.example.anbar.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses")
    fun getAll(): List<WarehouseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(items: List<WarehouseEntity>)

    @Query("DELETE FROM warehouses")
    fun clear()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(item: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(items: List<ProductEntity>)

    @Query("DELETE FROM products")
    fun clear()

    @Query("SELECT COUNT(*) FROM products")
    fun count(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAll(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(item: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(items: List<CustomerEntity>)

    @Query("DELETE FROM customers")
    fun clear()
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stock")
    fun getAll(): List<StockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(items: List<StockEntity>)

    @Query("DELETE FROM stock")
    fun clear()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAll(): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    fun getById(id: Long): SaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(item: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(items: List<SaleEntity>)

    @Query("DELETE FROM sales")
    fun clear()

    @Query("SELECT COUNT(*) FROM sales")
    fun count(): Int
}

@Dao
interface KvDao {
    @Query("SELECT value FROM kv WHERE key = :key LIMIT 1")
    fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(item: KvEntity)
}

@Dao
interface JsonStoreDao {
    @Query("SELECT json FROM json_store WHERE key = :key LIMIT 1")
    fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(item: JsonStoreEntity)

    @Query("SELECT key FROM json_store")
    fun keys(): List<String>
}
