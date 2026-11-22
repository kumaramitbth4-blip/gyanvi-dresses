package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleData
import com.example.data.local.AppManagementStorage
import com.example.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppManagementStorageTest {

    private lateinit var storage: AppManagementStorage

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        storage = AppManagementStorage(context)
        storage.clearAll()
    }

    @Test
    fun persistenceRoundTripsThemeLikhavatTextsImagesAndFeatures() {
        val customConfig = AppManagementConfig(
            themePreset = DashboardThemePreset.EMERALD_IVORY,
            homeLayoutMode = HomeLayoutMode.THREE_COLUMN,
            cardCornerPreset = CardCornerPreset.ROUNDED,
            texts = AppTextsConfig(
                brandTitle = "Gyanvi Royal Couture",
                announcementText = "Diwali Bumper Offer 80% Off",
                tabHomeLabel = "Start"
            ),
            features = AppFeaturesConfig(
                showHeroSlider = false,
                showVoiceSearch = false,
                showOrdersTab = false
            ),
            images = AppImagesConfig(
                brandAvatarCustomUri = "content://picker/logo.png",
                slide1CustomUri = "content://picker/slide1.jpg",
                slide2Enabled = false
            ),
            typography = AppTypographyConfig(
                fontStyle = AppFontStyle.PACIFICO_CURSIVE,
                fontScale = 1.2f,
                headingWeight = HeadingWeightStyle.EXTRA_BOLD,
                letterSpacing = LetterSpacingPreset.WIDE
            )
        )

        storage.saveConfig(customConfig)
        val restored = storage.loadConfig()

        assertEquals(DashboardThemePreset.EMERALD_IVORY, restored.themePreset)
        assertEquals(HomeLayoutMode.THREE_COLUMN, restored.homeLayoutMode)
        assertEquals(CardCornerPreset.ROUNDED, restored.cardCornerPreset)
        assertEquals("Gyanvi Royal Couture", restored.texts.brandTitle)
        assertEquals("Diwali Bumper Offer 80% Off", restored.texts.announcementText)
        assertEquals("Start", restored.texts.tabHomeLabel)
        assertFalse(restored.features.showHeroSlider)
        assertFalse(restored.features.showVoiceSearch)
        assertFalse(restored.features.showOrdersTab)
        assertEquals("content://picker/logo.png", restored.images.brandAvatarCustomUri)
        assertEquals("content://picker/slide1.jpg", restored.images.slide1CustomUri)
        assertFalse(restored.images.slide2Enabled)
        assertEquals(AppFontStyle.PACIFICO_CURSIVE, restored.typography.fontStyle)
        assertEquals(1.2f, restored.typography.fontScale, 0.001f)
        assertEquals(HeadingWeightStyle.EXTRA_BOLD, restored.typography.headingWeight)
        assertEquals(LetterSpacingPreset.WIDE, restored.typography.letterSpacing)
    }

    @Test
    fun categoryAndProductImageOverridesPersistCleanly() {
        val editedCats = SampleData.categoryList.dropLast(1) + CategoryItem(
            id = "cat_custom_1",
            name = "Party Wear",
            imageRes = R.drawable.dress_lehenga_1793417067159,
            offerBadge = "HOT",
            customImageUri = "content://picker/cat.jpg"
        )
        storage.saveCategories(editedCats)
        val loadedCats = storage.loadCategories(SampleData.categoryList)
        assertEquals(editedCats.size, loadedCats.size)
        assertEquals("Party Wear", loadedCats.last().name)
        assertEquals("content://picker/cat.jpg", loadedCats.last().customImageUri)

        val overrides = mapOf("gd_01" to "content://picker/kurti.jpg")
        storage.saveProductImageOverrides(overrides)
        assertEquals("content://picker/kurti.jpg", storage.loadProductImageOverrides()["gd_01"])
    }

    @Test
    fun backupSnapshotsPersistAndRestoreBundle() {
        val snapshot = AppBackupSnapshot(
            id = "bkp_test_1",
            label = "Festive Snapshot",
            createdAt = "14 Nov 2026, 08:15 PM",
            summary = "Royal Maroon • Outfit",
            themeName = "Royal Maroon",
            fontName = "Outfit",
            productCount = 12,
            categoryCount = 8
        )
        val cfg = AppManagementConfig(
            themePreset = DashboardThemePreset.ROYAL_MAROON_GOLD,
            features = AppFeaturesConfig(showFloatingQuickControls = false),
            texts = AppTextsConfig(brandTitle = "Gyanvi Gold Studio")
        )

        val jsonText = storage.saveBackupPayload(
            snapshot = snapshot,
            config = cfg,
            categories = SampleData.categoryList,
            productImageOverrides = mapOf("gd_01" to "content://picker/gold.jpg"),
            wishlistIds = setOf("gd_01", "gd_03"),
            deliveryAddress = "Patna - 800001"
        )
        assertTrue(jsonText.contains("Festive Snapshot"))

        val snapshots = storage.loadBackupSnapshots()
        assertEquals(1, snapshots.size)
        assertEquals("bkp_test_1", snapshots.first().id)

        val bundle = storage.loadBackupBundle("bkp_test_1")
        assertNotNull(bundle)
        assertEquals(DashboardThemePreset.ROYAL_MAROON_GOLD, bundle!!.config.themePreset)
        assertFalse(bundle.config.features.showFloatingQuickControls)
        assertEquals("Gyanvi Gold Studio", bundle.config.texts.brandTitle)
        assertEquals("Patna - 800001", bundle.deliveryAddress)
        assertTrue(bundle.wishlistIds.contains("gd_03"))
        assertEquals("content://picker/gold.jpg", bundle.productImageOverrides["gd_01"])

        storage.deleteBackup("bkp_test_1")
        assertTrue(storage.loadBackupSnapshots().isEmpty())
    }
}
