package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleData
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryManagementRepository
import com.example.data.local.GyanviDatabase
import com.example.data.local.ProductCatalogRepository
import com.example.data.local.ProductDao
import com.example.data.local.ProductEntity
import com.example.data.local.toEntity
import com.example.data.local.toProduct
import com.example.model.CategoryItem
import com.example.model.SubCategoryItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductRoomDatabaseTest {

    private lateinit var database: GyanviDatabase
    private lateinit var productDao: ProductDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: ProductCatalogRepository
    private lateinit var categoryRepository: CategoryManagementRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GyanviDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = database.productDao()
        categoryDao = database.categoryDao()
        repository = ProductCatalogRepository(productDao)
        categoryRepository = CategoryManagementRepository(categoryDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadProductEntityWithMrpSellingPriceAndStock() = runTest {
        val entity = ProductEntity(
            id = "offline_01",
            name = "Banarasi Silk Bridal Saree",
            category = "Sarees",
            subcategory = "Bridal Silk",
            mrp = 2999,
            sellingPrice = 999,
            stock = 45,
            fabric = "Pure Banarasi Silk",
            sizesCsv = "Free Size"
        )

        productDao.insertProduct(entity)

        val fetched = productDao.getProductById("offline_01")
        assertNotNull(fetched)
        assertEquals(2999, fetched?.mrp)
        assertEquals(999, fetched?.sellingPrice)
        assertEquals(45, fetched?.stock)
        assertTrue(fetched?.inStock == true)
        assertEquals(66, fetched?.discountPercent)

        val domainProduct = fetched!!.toProduct()
        assertEquals(999, domainProduct.price)
        assertEquals(2999, domainProduct.originalPrice)
    }

    @Test
    fun updateStockAndPricingPersistsOffline() = runTest {
        val entity = ProductEntity(
            id = "offline_02",
            name = "Designer Anarkali Kurti",
            category = "Kurtis & Suits",
            subcategory = "Anarkali",
            mrp = 1599,
            sellingPrice = 699,
            stock = 20,
            fabric = "Pure Georgette"
        )
        repository.insertOrUpdateProduct(entity)

        // Update stock to 0 -> inStock should become false
        repository.updateStock("offline_02", 0)
        val outOfStock = repository.getProductById("offline_02")
        assertEquals(0, outOfStock?.stock)
        assertFalse(outOfStock?.inStock ?: true)

        val inStockList = productDao.getInStockProducts().first()
        assertTrue(inStockList.none { it.id == "offline_02" })

        val lowStockList = productDao.getLowStockProducts(threshold = 5).first()
        assertTrue(lowStockList.any { it.id == "offline_02" })

        // Update MRP and Selling Price
        repository.updatePricing("offline_02", mrp = 1799, sellingPrice = 599)
        val repriced = repository.getProductById("offline_02")
        assertEquals(1799, repriced?.mrp)
        assertEquals(599, repriced?.sellingPrice)
    }

    @Test
    fun seedInitialCatalogSearchCategoryAndDelete() = runTest {
        repository.seedInitialCatalogIfEmpty(SampleData.sampleProducts)
        val allCached = repository.allCachedProducts.first()
        assertEquals(SampleData.sampleProducts.size, allCached.size)

        val sarees = repository.getProductsByCategory("Sarees").first()
        assertTrue(sarees.isNotEmpty())
        assertTrue(sarees.all { it.category == "Sarees" || it.subcategory == "Sarees" })

        val searchResults = repository.searchProducts("Banarasi").first()
        assertTrue(searchResults.isNotEmpty())

        val firstId = allCached.first().id
        repository.deleteProductById(firstId)
        assertNull(repository.getProductById(firstId))
    }

    @Test
    fun ethnicWearProductSchemaPersistsNamePriceDescriptionCategoryAndImageUrl() = runTest {
        val ethnicProduct = com.example.model.Product(
            id = "eth_banarasi_01",
            name = "Royal Zari Banarasi Anarkali Gown",
            category = "Ethnic Gowns",
            subcategory = "Anarkali",
            price = 1499,
            originalPrice = 2999,
            rating = 4.8f,
            ratingCount = 240,
            imageRes = com.example.R.drawable.dress_anarkali_1793417013639,
            fabric = "Pure Banarasi Silk",
            description = "Handcrafted festive Banarasi silk gown with antique zari border and dupatta.",
            imageUrl = "https://cdn.gyanvidresses.in/images/banarasi-anarkali-01.jpg"
        )

        repository.upsertDomainProduct(ethnicProduct, stock = 32, sku = "GYN-ETH-001")

        val storedEntity = repository.getProductById("eth_banarasi_01")
        assertNotNull(storedEntity)
        assertEquals("Royal Zari Banarasi Anarkali Gown", storedEntity?.name)
        assertEquals(1499, storedEntity?.sellingPrice)
        assertEquals("Ethnic Gowns", storedEntity?.category)
        assertEquals(
            "Handcrafted festive Banarasi silk gown with antique zari border and dupatta.",
            storedEntity?.description
        )
        assertEquals("https://cdn.gyanvidresses.in/images/banarasi-anarkali-01.jpg", storedEntity?.imageUrl)

        val roundTripped = storedEntity!!.toProduct()
        assertEquals(ethnicProduct.name, roundTripped.name)
        assertEquals(ethnicProduct.price, roundTripped.price)
        assertEquals(ethnicProduct.description, roundTripped.description)
        assertEquals(ethnicProduct.category, roundTripped.category)
        assertEquals(ethnicProduct.imageUrl, roundTripped.imageUrl)
        assertEquals(ethnicProduct.imageUrl, roundTripped.resolvedImageUrl)

        // Description search & distinct category query
        val byDescription = repository.searchProducts("antique zari").first()
        assertTrue(byDescription.any { it.id == "eth_banarasi_01" })
        val categories = repository.distinctCategories.first()
        assertTrue(categories.contains("Ethnic Gowns"))

        // Pause / Activate toggle in Room
        repository.setProductPaused("eth_banarasi_01", true)
        assertTrue(repository.pausedCatalogEntities.first().any { it.id == "eth_banarasi_01" })
        assertTrue(repository.activeCatalogProducts.first().none { it.id == "eth_banarasi_01" })

        repository.setProductPaused("eth_banarasi_01", false)
        assertTrue(repository.activeCatalogProducts.first().any { it.id == "eth_banarasi_01" })
    }

    @Test
    fun separatedCategoryAndSubCategoryTablesAndMultiCategoryProductsPersist() = runTest {
        categoryRepository.seedDefaultsIfEmpty(
            defaultCategories = SampleData.categoryList,
            defaultSubCategories = SampleData.defaultSubCategories
        )

        val storedCategories = categoryRepository.allCategories.first()
        val storedSubCategories = categoryRepository.allSubCategories.first()
        assertEquals(SampleData.categoryList.size, storedCategories.size)
        assertTrue(storedCategories.size >= 32)
        assertEquals(storedCategories.size, storedCategories.map { it.name.trim().lowercase() }.distinct().size)
        assertTrue(storedSubCategories.any { it.parentCategory == "Women" && it.name == "Saree" })
        assertTrue(storedSubCategories.any { it.parentCategory == "Men" && it.name == "Kurta" })
        assertTrue(storedSubCategories.any { it.parentCategory == "Kids & Baby" && it.name == "Baby Frock" })
        assertTrue(storedSubCategories.any { it.parentCategory == "Footwear" && it.name == "Men Shoes" })
        assertTrue(storedSubCategories.any { it.parentCategory == "Footwear" && it.name == "Women Shoes" })

        // Upsert new custom category + subcategory, deactivate, and cascade delete
        val customCat = CategoryItem(
            id = "CAT-BRIDE-99",
            name = "Bridal Special",
            imageRes = com.example.R.drawable.dress_anarkali_1793417013639,
            offerBadge = "New",
            seoName = "bridal-special",
            isActive = true,
            sortOrder = 99
        )
        categoryRepository.upsertCategory(customCat)
        categoryRepository.upsertSubCategory(
            SubCategoryItem(
                id = "SUB-BRIDE-1",
                parentCategory = "Bridal Special",
                name = "Reception Lehenga",
                imageRes = com.example.R.drawable.dress_anarkali_1793417013639,
                parentCategoryId = "CAT-BRIDE-99",
                seoName = "reception-lehenga",
                isActive = true,
                sortOrder = 1
            )
        )
        assertTrue(categoryRepository.allCategories.first().any { it.id == "CAT-BRIDE-99" })
        assertEquals(1, categoryRepository.getSubCategoriesForParent("CAT-BRIDE-99").first().size)

        // Inactive category must be excluded from activeCategories Flow
        categoryRepository.upsertCategory(customCat.copy(isActive = false))
        assertFalse(categoryRepository.activeCategories.first().any { it.id == "CAT-BRIDE-99" })

        // Deleting parent category deletes its child subcategories
        categoryRepository.deleteCategory("CAT-BRIDE-99")
        assertFalse(categoryRepository.allCategories.first().any { it.id == "CAT-BRIDE-99" })
        assertTrue(categoryRepository.getSubCategoriesForParent("CAT-BRIDE-99").first().isEmpty())

        // Multi-category Product persistence
        val multiCatProduct = SampleData.sampleProducts.first().copy(
            id = "multi_cat_01",
            category = "Women",
            subcategory = "Saree",
            categoryId = "cat_women",
            subcategoryId = "sub_women_saree",
            additionalCategories = listOf("Best Sellers", "Offers & Discounts", "Bridal Boutique")
        )
        repository.insertOrUpdateProduct(multiCatProduct.toEntity())
        val loaded = repository.getProductById("multi_cat_01")!!.toProduct()
        assertEquals("cat_women", loaded.categoryId)
        assertEquals("sub_women_saree", loaded.subcategoryId)
        assertTrue(loaded.matchesCategory("Women"))
        assertTrue(loaded.matchesCategory("Saree"))
        assertTrue(loaded.matchesCategory("Best Sellers"))
        assertTrue(loaded.matchesCategory("Bridal Boutique"))
    }

    @Test
    fun roomV5CatalogUploadSchemaPersistsColorWeightNumericSizesAndExtraImages() = runTest {
        val uploadedShoes = com.example.model.Product(
            id = "ftw_men_oxford_99",
            name = "Men's Royal Tan Formal Oxford Shoes",
            category = "Footwear",
            subcategory = "Men Shoes",
            additionalCategories = listOf("Men", "New Arrivals"),
            price = 799,
            originalPrice = 2499,
            rating = 4.7f,
            ratingCount = 320,
            imageRes = com.example.R.drawable.dress_anarkali_1793417013639,
            fabric = "Genuine Leather & TPR Sole",
            sizes = listOf("UK 6", "UK 7", "UK 8", "UK 9", "UK 10"),
            inStock = true,
            description = "Handcrafted stitched leather formal shoes for men.",
            imageUrl = "https://cdn.gyanvidresses.in/footwear/oxford-1.jpg",
            brandName = "GYANVI FOOTWEAR",
            color = "Tan Brown",
            weight = "680g",
            numericSizes = listOf("40", "41", "42"),
            extraImageUrls = listOf(
                "https://cdn.gyanvidresses.in/footwear/oxford-1.jpg",
                "https://cdn.gyanvidresses.in/footwear/oxford-2.jpg"
            ),
            sku = "GYN-FTW-OX-99"
        )

        repository.insertOrUpdateProduct(uploadedShoes.toEntity(stock = 40, sku = "GYN-FTW-OX-99", brandName = "GYANVI FOOTWEAR"))
        val entity = repository.getProductById("ftw_men_oxford_99")
        assertNotNull(entity)
        assertEquals("Tan Brown", entity?.color)
        assertEquals("680g", entity?.weight)
        assertEquals("GYANVI FOOTWEAR", entity?.brandName)

        val roundTripped = entity!!.toProduct()
        assertEquals("Tan Brown", roundTripped.color)
        assertEquals("680g", roundTripped.weight)
        assertEquals(listOf("40", "41", "42"), roundTripped.numericSizes)
        assertEquals(2, roundTripped.extraImageUrls.size)
        assertEquals("GYN-FTW-OX-99", roundTripped.sku)

        val byAdditionalCategory = repository.getProductsByCategory("New Arrivals").first()
        assertTrue(byAdditionalCategory.any { it.id == "ftw_men_oxford_99" })
        val byColorSearch = repository.searchProducts("Tan Brown").first()
        assertTrue(byColorSearch.any { it.id == "ftw_men_oxford_99" })
    }
}

