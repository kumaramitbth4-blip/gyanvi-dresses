package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.model.CategoryItem
import com.example.model.SubCategoryItem
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["sort_order"]),
        Index(value = ["name"])
    ]
)
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "image_res")
    val imageRes: Int,

    @ColumnInfo(name = "offer_badge")
    val offerBadge: String = "",

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",

    @ColumnInfo(name = "seo_name")
    val seoName: String = "",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)

@Entity(
    tableName = "subcategories",
    indices = [
        Index(value = ["parent_category_id"]),
        Index(value = ["sort_order"])
    ]
)
data class SubCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "parent_category_id")
    val parentCategoryId: String,

    @ColumnInfo(name = "parent_category")
    val parentCategory: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "image_res")
    val imageRes: Int,

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",

    @ColumnInfo(name = "seo_name")
    val seoName: String = "",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort_order ASC, name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM subcategories ORDER BY sort_order ASC, name ASC")
    fun getAllSubCategoriesFlow(): Flow<List<SubCategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT COUNT(*) FROM subcategories")
    suspend fun getSubCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)

    @Query("DELETE FROM subcategories WHERE parent_category_id = :parentId")
    suspend fun deleteSubCategoriesForParent(parentId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubCategory(subCategory: SubCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubCategories(subCategories: List<SubCategoryEntity>)

    @Query("DELETE FROM subcategories WHERE id = :id")
    suspend fun deleteSubCategory(id: String)

    @Query("DELETE FROM subcategories")
    suspend fun deleteAllSubCategories()
}

fun CategoryEntity.toModel(): CategoryItem = CategoryItem(
    id = id,
    name = name,
    imageRes = imageRes,
    offerBadge = offerBadge,
    customImageUri = imageUrl.takeIf { it.isNotBlank() },
    seoName = seoName.ifBlank { name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-') },
    isActive = isActive,
    sortOrder = sortOrder
)

fun CategoryItem.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    imageRes = imageRes,
    offerBadge = offerBadge,
    imageUrl = customImageUri.orEmpty(),
    seoName = seoName,
    isActive = isActive,
    sortOrder = sortOrder
)

fun SubCategoryEntity.toModel(): SubCategoryItem = SubCategoryItem(
    id = id,
    parentCategory = parentCategory,
    name = name,
    imageRes = imageRes,
    parentCategoryId = parentCategoryId,
    customImageUri = imageUrl.takeIf { it.isNotBlank() },
    seoName = seoName.ifBlank { name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-') },
    isActive = isActive,
    sortOrder = sortOrder
)

fun SubCategoryItem.toEntity(): SubCategoryEntity = SubCategoryEntity(
    id = id,
    parentCategoryId = parentCategoryId,
    parentCategory = parentCategory,
    name = name,
    imageRes = imageRes,
    imageUrl = customImageUri.orEmpty(),
    seoName = seoName,
    isActive = isActive,
    sortOrder = sortOrder
)
