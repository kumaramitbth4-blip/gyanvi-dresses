package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.Product

/**
 * Room Entity representing an ethnic wear catalog product stored locally in SQLite
 * for offline inventory management. Includes name, price/MRP, description, category,
 * image URL, and stock quantity columns.
 */
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "subcategory")
    val subcategory: String,

    @ColumnInfo(name = "mrp")
    val mrp: Int,

    @ColumnInfo(name = "selling_price")
    val sellingPrice: Int,

    @ColumnInfo(name = "stock")
    val stock: Int,

    @ColumnInfo(name = "in_stock")
    val inStock: Boolean = stock > 0,

    @ColumnInfo(name = "sku")
    val sku: String = "GYANVI-${id.uppercase()}",

    @ColumnInfo(name = "brand_name")
    val brandName: String = "GYANVI DRESSES",

    @ColumnInfo(name = "fabric")
    val fabric: String,

    @ColumnInfo(name = "sizes_csv")
    val sizesCsv: String = "S,M,L,XL,XXL",

    @ColumnInfo(name = "rating")
    val rating: Float = 4.5f,

    @ColumnInfo(name = "rating_count")
    val ratingCount: Int = 0,

    @ColumnInfo(name = "image_res")
    val imageRes: Int = 0,

    @ColumnInfo(name = "image_url", defaultValue = "")
    val imageUrl: String = "",

    @ColumnInfo(name = "is_maha_deal")
    val isMahaDeal: Boolean = false,

    @ColumnInfo(name = "is_mall_assured")
    val isMallAssured: Boolean = true,

    @ColumnInfo(name = "is_paused", defaultValue = "0")
    val isPaused: Boolean = false,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "category_id", defaultValue = "")
    val categoryId: String = "",

    @ColumnInfo(name = "subcategory_id", defaultValue = "")
    val subcategoryId: String = "",

    @ColumnInfo(name = "additional_categories_csv", defaultValue = "")
    val additionalCategoriesCsv: String = "",

    @ColumnInfo(name = "color", defaultValue = "")
    val color: String = "",

    @ColumnInfo(name = "weight", defaultValue = "350g")
    val weight: String = "350g",

    @ColumnInfo(name = "numeric_sizes_csv", defaultValue = "")
    val numericSizesCsv: String = "",

    @ColumnInfo(name = "extra_images_csv", defaultValue = "")
    val extraImagesCsv: String = "",

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    val discountPercent: Int
        get() = if (mrp > sellingPrice && mrp > 0) {
            (((mrp - sellingPrice).toFloat() / mrp) * 100).toInt()
        } else 0
}

fun ProductEntity.toProduct(): Product {
    val parsedSizes = sizesCsv
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .ifEmpty { listOf("Free Size") }

    val cleanImageUrl = imageUrl.trim()
    val extraCategories = additionalCategoriesCsv
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val parsedNumericSizes = numericSizesCsv
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val parsedExtraImages = extraImagesCsv
        .split("|")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return Product(
        id = id,
        name = name,
        category = category,
        subcategory = subcategory,
        price = sellingPrice,
        originalPrice = mrp,
        rating = rating,
        ratingCount = ratingCount,
        imageRes = imageRes,
        fabric = fabric,
        sizes = parsedSizes,
        isMahaDeal = isMahaDeal,
        isMallAssured = isMallAssured,
        inStock = inStock && stock > 0,
        codPrice = sellingPrice + 29,
        upiPrice = sellingPrice,
        description = description,
        imageUrl = cleanImageUrl,
        customImageUri = cleanImageUrl.takeIf { it.isNotEmpty() },
        categoryId = categoryId,
        subcategoryId = subcategoryId,
        additionalCategories = extraCategories,
        brandName = brandName.ifBlank { "GYANVI DRESSES" },
        color = color,
        weight = weight.ifBlank { "350g" },
        numericSizes = parsedNumericSizes,
        extraImageUrls = parsedExtraImages,
        sku = sku
    )
}

fun Product.toEntity(
    stock: Int = if (inStock) 100 else 0,
    sku: String = this.sku.ifBlank { "GYANVI-${id.uppercase()}" },
    brandName: String = this.brandName.ifBlank { "GYANVI DRESSES" },
    isPaused: Boolean = false
): ProductEntity {
    val resolvedUrl = resolvedImageUrl.trim()
    return ProductEntity(
        id = id,
        name = name,
        category = category,
        subcategory = subcategory,
        mrp = originalPrice,
        sellingPrice = price,
        stock = stock.coerceAtLeast(0),
        inStock = inStock && stock > 0,
        sku = sku,
        brandName = brandName,
        fabric = fabric,
        sizesCsv = sizes.joinToString(","),
        rating = rating,
        ratingCount = ratingCount,
        imageRes = imageRes,
        imageUrl = resolvedUrl,
        isMahaDeal = isMahaDeal,
        isMallAssured = isMallAssured,
        isPaused = isPaused,
        description = description,
        categoryId = categoryId,
        subcategoryId = subcategoryId,
        additionalCategoriesCsv = additionalCategories.joinToString(","),
        color = color,
        weight = weight.ifBlank { "350g" },
        numericSizesCsv = numericSizes.joinToString(","),
        extraImagesCsv = extraImageUrls.joinToString("|")
    )
}
