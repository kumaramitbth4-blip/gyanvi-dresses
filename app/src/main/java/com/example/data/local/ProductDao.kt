package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for managing local offline storage of ethnic wear inventory products
 * (name, price, MRP, description, category, image URL, and stock).
 */
@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY updated_at DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE is_paused = 0 ORDER BY updated_at DESC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE is_paused = 1 ORDER BY updated_at DESC")
    fun getPausedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getDistinctCategories(): Flow<List<String>>

    @Query(
        "SELECT * FROM products WHERE category = :category " +
            "OR subcategory = :category " +
            "OR additional_categories_csv LIKE '%' || :category || '%' " +
            "ORDER BY updated_at DESC"
    )
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query(
        "SELECT * FROM products WHERE name LIKE '%' || :query || '%' " +
            "OR category LIKE '%' || :query || '%' " +
            "OR subcategory LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%' " +
            "OR fabric LIKE '%' || :query || '%' " +
            "OR brand_name LIKE '%' || :query || '%' " +
            "OR color LIKE '%' || :query || '%' " +
            "OR sku LIKE '%' || :query || '%' " +
            "ORDER BY updated_at DESC"
    )
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock > 0 AND is_paused = 0 ORDER BY updated_at DESC")
    fun getInStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= :threshold ORDER BY stock ASC")
    fun getLowStockProducts(threshold: Int = 10): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query(
        "UPDATE products SET stock = :newStock, in_stock = (:newStock > 0), updated_at = :timestamp " +
            "WHERE id = :productId"
    )
    suspend fun updateStock(
        productId: String,
        newStock: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query(
        "UPDATE products SET mrp = :newMrp, selling_price = :newSellingPrice, updated_at = :timestamp " +
            "WHERE id = :productId"
    )
    suspend fun updatePricing(
        productId: String,
        newMrp: Int,
        newSellingPrice: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query(
        "UPDATE products SET is_paused = :paused, updated_at = :timestamp " +
            "WHERE id = :productId"
    )
    suspend fun setProductPaused(
        productId: String,
        paused: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()
}
