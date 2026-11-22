package com.example.data.local

import com.example.model.CategoryItem
import com.example.model.SubCategoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryManagementRepository(private val dao: CategoryDao) {

    val allCategories: Flow<List<CategoryItem>> =
        dao.getAllCategoriesFlow().map { entities -> entities.map { it.toModel() } }

    val activeCategories: Flow<List<CategoryItem>> =
        allCategories.map { categories -> categories.filter { it.isActive } }

    val allSubCategories: Flow<List<SubCategoryItem>> =
        dao.getAllSubCategoriesFlow().map { entities -> entities.map { it.toModel() } }

    fun getSubCategoriesForParent(parentId: String): Flow<List<SubCategoryItem>> =
        allSubCategories.map { subs ->
            subs.filter {
                it.parentCategoryId.equals(parentId, ignoreCase = true) ||
                    it.parentCategory.equals(parentId, ignoreCase = true)
            }
        }

    suspend fun seedDefaultsIfEmpty(
        defaultCategories: List<CategoryItem>,
        defaultSubCategories: List<SubCategoryItem>
    ) {
        if (dao.getCategoryCount() == 0) {
            dao.upsertCategories(defaultCategories.map { it.toEntity() })
        }
        if (dao.getSubCategoryCount() == 0) {
            dao.upsertSubCategories(defaultSubCategories.map { it.toEntity() })
        }
    }

    suspend fun upsertCategory(category: CategoryItem) {
        dao.upsertCategory(category.toEntity())
    }

    suspend fun deleteCategory(categoryId: String) {
        dao.deleteCategory(categoryId)
        dao.deleteSubCategoriesForParent(categoryId)
    }

    suspend fun replaceAllCategories(categories: List<CategoryItem>) {
        dao.deleteAllCategories()
        dao.upsertCategories(categories.map { it.toEntity() })
    }

    suspend fun upsertSubCategory(subCategory: SubCategoryItem) {
        dao.upsertSubCategory(subCategory.toEntity())
    }

    suspend fun deleteSubCategory(subCategoryId: String) {
        dao.deleteSubCategory(subCategoryId)
    }

    suspend fun replaceAllSubCategories(subCategories: List<SubCategoryItem>) {
        dao.deleteAllSubCategories()
        dao.upsertSubCategories(subCategories.map { it.toEntity() })
    }
}
