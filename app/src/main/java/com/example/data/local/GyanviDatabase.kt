package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Local Room SQLite Database for separated Categories, Subcategories, Products, and User Accounts.
 */
@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        SubCategoryEntity::class,
        UserAccountEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class GyanviDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: GyanviDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN image_url TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN is_paused INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_products_category ON products(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_products_name ON products(name)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN category_id TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN subcategory_id TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN additional_categories_csv TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        image_res INTEGER NOT NULL,
                        offer_badge TEXT NOT NULL,
                        image_url TEXT NOT NULL,
                        seo_name TEXT NOT NULL,
                        is_active INTEGER NOT NULL,
                        sort_order INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_sort_order ON categories(sort_order)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_name ON categories(name)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS subcategories (
                        id TEXT NOT NULL PRIMARY KEY,
                        parent_category_id TEXT NOT NULL,
                        parent_category TEXT NOT NULL,
                        name TEXT NOT NULL,
                        image_res INTEGER NOT NULL,
                        image_url TEXT NOT NULL,
                        seo_name TEXT NOT NULL,
                        is_active INTEGER NOT NULL,
                        sort_order INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subcategories_parent_category_id ON subcategories(parent_category_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subcategories_sort_order ON subcategories(sort_order)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN color TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN weight TEXT NOT NULL DEFAULT '350g'")
                db.execSQL("ALTER TABLE products ADD COLUMN numeric_sizes_csv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE products ADD COLUMN extra_images_csv TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): GyanviDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GyanviDatabase::class.java,
                    "gyanvi_catalog.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
