package com.example.data.local

import com.example.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository mediating between Room's local ProductDao and the ViewModel layer.
 */
class ProductCatalogRepository(private val productDao: ProductDao) {

    val allCachedProducts: Flow<List<Product>> = productDao.getAllProducts()
        .map { entities -> entities.map { it.toProduct() } }

    val allCachedEntities: Flow<List<ProductEntity>> = productDao.getAllProducts()

    val activeCatalogProducts: Flow<List<Product>> = productDao.getActiveProducts()
        .map { entities -> entities.map { it.toProduct() } }

    val pausedCatalogEntities: Flow<List<ProductEntity>> = productDao.getPausedProducts()

    val distinctCategories: Flow<List<String>> = productDao.getDistinctCategories()

    fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getProductsByCategory(category).map { entities -> entities.map { it.toProduct() } }

    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query).map { entities -> entities.map { it.toProduct() } }

    fun getLowStockProducts(threshold: Int = 10): Flow<List<ProductEntity>> =
        productDao.getLowStockProducts(threshold)

    suspend fun getProductById(productId: String): ProductEntity? =
        productDao.getProductById(productId)

    suspend fun seedInitialCatalogIfEmpty(products: List<Product>) {
        if (productDao.getProductCount() == 0 && products.isNotEmpty()) {
            productDao.insertProducts(products.map { it.toEntity() })
        }
    }

    suspend fun upsertDomainProduct(
        product: Product,
        stock: Int = if (product.inStock) 100 else 0,
        sku: String = "GYANVI-${product.id.uppercase()}",
        isPaused: Boolean = false
    ) {
        productDao.insertProduct(product.toEntity(stock = stock, sku = sku, isPaused = isPaused))
    }

    suspend fun insertOrUpdateProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun insertOrUpdateProducts(products: List<ProductEntity>) {
        productDao.insertProducts(products)
    }

    suspend fun updateStock(productId: String, newStock: Int) {
        productDao.updateStock(productId, newStock.coerceAtLeast(0))
    }

    suspend fun updatePricing(productId: String, mrp: Int, sellingPrice: Int) {
        productDao.updatePricing(productId, mrp.coerceAtLeast(1), sellingPrice.coerceAtLeast(1))
    }

    suspend fun setProductPaused(productId: String, paused: Boolean) {
        productDao.setProductPaused(productId, paused)
    }

    suspend fun deleteProductById(productId: String) {
        productDao.deleteProductById(productId)
    }
}
