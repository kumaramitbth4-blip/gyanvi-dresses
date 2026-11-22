package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class AppManagementStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gyanvi_app_management_prefs", Context.MODE_PRIVATE)

    fun loadConfig(): AppManagementConfig {
        val raw = prefs.getString(KEY_CONFIG_JSON, null) ?: return AppManagementConfig()
        return runCatching { decodeConfig(raw) }.getOrDefault(AppManagementConfig())
    }

    fun saveConfig(config: AppManagementConfig) {
        runCatching {
            prefs.edit().putString(KEY_CONFIG_JSON, encodeConfig(config)).apply()
        }
    }

    fun loadCategories(defaults: List<CategoryItem>): List<CategoryItem> {
        val raw = prefs.getString(KEY_CATEGORIES_JSON, null) ?: return defaults
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<CategoryItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val catName = obj.getString("name")
                list.add(
                    CategoryItem(
                        id = obj.getString("id"),
                        name = catName,
                        imageRes = obj.optInt("imageRes", R.drawable.dress_anarkali_1793417013639),
                        offerBadge = obj.optString("offerBadge", ""),
                        customImageUri = obj.optString("customImageUri", "").ifBlank { null },
                        seoName = obj.optString("seoName", catName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')),
                        isActive = obj.optBoolean("isActive", true),
                        sortOrder = obj.optInt("sortOrder", i + 1)
                    )
                )
            }
            list
        }.getOrDefault(defaults)
    }

    fun saveCategories(categories: List<CategoryItem>) {
        runCatching {
            val arr = JSONArray()
            categories.forEach { cat ->
                arr.put(JSONObject().apply {
                    put("id", cat.id)
                    put("name", cat.name)
                    put("imageRes", cat.imageRes)
                    put("offerBadge", cat.offerBadge)
                    put("customImageUri", cat.customImageUri ?: "")
                    put("seoName", cat.seoName)
                    put("isActive", cat.isActive)
                    put("sortOrder", cat.sortOrder)
                })
            }
            prefs.edit().putString(KEY_CATEGORIES_JSON, arr.toString()).apply()
        }
    }

    fun loadSubCategories(defaults: List<SubCategoryItem>): List<SubCategoryItem> {
        val raw = prefs.getString(KEY_SUBCATEGORIES_JSON, null) ?: return defaults
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<SubCategoryItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val subName = obj.getString("name")
                list.add(
                    SubCategoryItem(
                        id = obj.getString("id"),
                        parentCategory = obj.getString("parentCategory"),
                        name = subName,
                        imageRes = obj.optInt("imageRes", R.drawable.dress_anarkali_1793417013639),
                        parentCategoryId = obj.optString("parentCategoryId", ""),
                        customImageUri = obj.optString("customImageUri", "").ifBlank { null },
                        seoName = obj.optString("seoName", subName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')),
                        isActive = obj.optBoolean("isActive", true),
                        sortOrder = obj.optInt("sortOrder", i + 1)
                    )
                )
            }
            list
        }.getOrDefault(defaults)
    }

    fun saveSubCategories(subCategories: List<SubCategoryItem>) {
        runCatching {
            val arr = JSONArray()
            subCategories.forEach { sub ->
                arr.put(JSONObject().apply {
                    put("id", sub.id)
                    put("parentCategoryId", sub.parentCategoryId)
                    put("parentCategory", sub.parentCategory)
                    put("name", sub.name)
                    put("imageRes", sub.imageRes)
                    put("customImageUri", sub.customImageUri ?: "")
                    put("seoName", sub.seoName)
                    put("isActive", sub.isActive)
                    put("sortOrder", sub.sortOrder)
                })
            }
            prefs.edit().putString(KEY_SUBCATEGORIES_JSON, arr.toString()).apply()
        }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun loadProductImageOverrides(): Map<String, String> {
        val raw = prefs.getString(KEY_PRODUCT_IMAGES_JSON, null) ?: return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { key ->
                val v = obj.optString(key, "")
                if (v.isNotBlank()) map[key] = v
            }
            map
        }.getOrDefault(emptyMap())
    }

    fun saveProductImageOverrides(overrides: Map<String, String>) {
        runCatching {
            val obj = JSONObject()
            overrides.forEach { (k, v) -> if (v.isNotBlank()) obj.put(k, v) }
            prefs.edit().putString(KEY_PRODUCT_IMAGES_JSON, obj.toString()).apply()
        }
    }

    private fun encodeConfig(config: AppManagementConfig): String {
        val root = JSONObject()
        root.put("themePreset", config.themePreset.name)
        root.put("homeLayoutMode", config.homeLayoutMode.name)
        root.put("cardCornerPreset", config.cardCornerPreset.name)

        root.put("texts", JSONObject().apply {
            val t = config.texts
            put("brandTitle", t.brandTitle)
            put("brandHindiTitle", t.brandHindiTitle)
            put("searchPlaceholder", t.searchPlaceholder)
            put("deliveryPrefix", t.deliveryPrefix)
            put("announcementText", t.announcementText)
            put("saleBadgeText", t.saleBadgeText)
            put("saleTimerLabel", t.saleTimerLabel)
            put("slide2Badge", t.slide2Badge)
            put("slide2Title", t.slide2Title)
            put("slide2Subtitle", t.slide2Subtitle)
            put("slide3Badge", t.slide3Badge)
            put("slide3Title", t.slide3Title)
            put("slide3Subtitle", t.slide3Subtitle)
            put("devProgressTitle", t.devProgressTitle)
            put("devProgressSubtitle", t.devProgressSubtitle)
            put("devProgressCta", t.devProgressCta)
            put("dealsHeadingFirst", t.dealsHeadingFirst)
            put("dealsHeadingSecond", t.dealsHeadingSecond)
            put("tabHomeLabel", t.tabHomeLabel)
            put("tabCategoriesLabel", t.tabCategoriesLabel)
            put("tabMallLabel", t.tabMallLabel)
            put("tabVideoFindsLabel", t.tabVideoFindsLabel)
            put("tabOrdersLabel", t.tabOrdersLabel)
            put("addToCartButtonText", t.addToCartButtonText)
            put("footerText", t.footerText)
        })

        root.put("features", JSONObject().apply {
            val f = config.features
            put("showHeroSlider", f.showHeroSlider)
            put("showAnnouncementTicker", f.showAnnouncementTicker)
            put("showCountdownBanner", f.showCountdownBanner)
            put("showDevProgressStrip", f.showDevProgressStrip)
            put("showCategoryStrip", f.showCategoryStrip)
            put("showFilterBar", f.showFilterBar)
            put("showVoiceSearch", f.showVoiceSearch)
            put("showCameraSearch", f.showCameraSearch)
            put("showWishlistHearts", f.showWishlistHearts)
            put("showShareButtons", f.showShareButtons)
            put("showAddToCartButtons", f.showAddToCartButtons)
            put("showSaleBadges", f.showSaleBadges)
            put("showMallBadges", f.showMallBadges)
            put("showCategoriesTab", f.showCategoriesTab)
            put("showMallTab", f.showMallTab)
            put("showVideoFindsTab", f.showVideoFindsTab)
            put("showOrdersTab", f.showOrdersTab)
            put("showQuickEditShortcut", f.showQuickEditShortcut)
            put("showFloatingQuickControls", f.showFloatingQuickControls)
        })

        root.put("images", JSONObject().apply {
            val im = config.images
            put("brandAvatarCustomUri", im.brandAvatarCustomUri ?: "")
            put("brandAvatarDrawable", im.brandAvatarDrawable ?: -1)
            put("slide1Enabled", im.slide1Enabled)
            put("slide1CustomUri", im.slide1CustomUri ?: "")
            put("slide1Drawable", im.slide1Drawable)
            put("slide2Enabled", im.slide2Enabled)
            put("slide2CustomUri", im.slide2CustomUri ?: "")
            put("slide2Drawable", im.slide2Drawable)
            put("slide3Enabled", im.slide3Enabled)
            put("slide3CustomUri", im.slide3CustomUri ?: "")
            put("slide3Drawable", im.slide3Drawable ?: -1)
        })

        root.put("typography", JSONObject().apply {
            val ty = config.typography
            put("fontStyle", ty.fontStyle.name)
            put("fontScale", ty.fontScale.toDouble())
            put("headingWeight", ty.headingWeight.name)
            put("letterSpacing", ty.letterSpacing.name)
        })

        root.put("paymentGateways", JSONObject().apply {
            val pg = config.paymentGateways
            put("activeGatewayId", pg.activeGatewayId ?: "")
            put("appendOrderParams", pg.appendOrderParams)
            put("hasSeededDefaults", pg.hasSeededDefaults)
            put("gateways", JSONArray().apply {
                pg.gateways.forEach { gw ->
                    put(JSONObject().apply {
                        put("id", gw.id)
                        put("name", gw.name)
                        put("url", gw.url)
                        put("provider", gw.provider.name)
                        put("isEnabled", gw.isEnabled)
                        put("lastStatus", gw.lastStatus)
                    })
                }
            })
        })

        root.put("socialAds", JSONObject().apply {
            val sa = config.socialAds
            put("adsEnabled", sa.adsEnabled)
            put("showOnHome", sa.showOnHome)
            put("campaigns", JSONArray().apply {
                sa.campaigns.forEach { ad ->
                    put(JSONObject().apply {
                        put("id", ad.id)
                        put("title", ad.title)
                        put("platform", ad.platform.name)
                        put("headline", ad.headline)
                        put("caption", ad.caption)
                        put("targetUrl", ad.targetUrl)
                        put("dailyBudgetInr", ad.dailyBudgetInr)
                        put("impressions", ad.impressions)
                        put("clicks", ad.clicks)
                        put("ordersGenerated", ad.ordersGenerated)
                        put("isActive", ad.isActive)
                        put("showOnHomeBanner", ad.showOnHomeBanner)
                        put("ctaLabel", ad.ctaLabel)
                    })
                }
            })
        })

        root.put("websiteIntegration", JSONObject().apply {
            val wi = config.websiteIntegration
            put("websiteDomain", wi.websiteDomain)
            put("webStoreTitle", wi.webStoreTitle)
            put("webCheckoutSyncEnabled", wi.webCheckoutSyncEnabled)
            put("deepLinkBaseUri", wi.deepLinkBaseUri)
            put("maintenanceMode", wi.maintenanceMode)
            put("termsAndConditions", wi.termsAndConditions)
            put("privacyPolicy", wi.privacyPolicy)
        })

        root.put("commissions", JSONObject().apply {
            val cm = config.commissions
            put("globalPercent", cm.globalPercent.toDouble())
            put("sareePercent", cm.sareePercent.toDouble())
            put("kurtiPercent", cm.kurtiPercent.toDouble())
            put("lehengaPercent", cm.lehengaPercent.toDouble())
            put("kidsPercent", cm.kidsPercent.toDouble())
        })

        return root.toString()
    }

    private fun decodeConfig(raw: String): AppManagementConfig {
        val root = JSONObject(raw)
        val defaults = AppManagementConfig()
        val themePreset = runCatching {
            DashboardThemePreset.valueOf(root.optString("themePreset", defaults.themePreset.name))
        }.getOrDefault(defaults.themePreset)
        val homeLayout = runCatching {
            HomeLayoutMode.valueOf(root.optString("homeLayoutMode", defaults.homeLayoutMode.name))
        }.getOrDefault(defaults.homeLayoutMode)
        val cardCorner = runCatching {
            CardCornerPreset.valueOf(root.optString("cardCornerPreset", defaults.cardCornerPreset.name))
        }.getOrDefault(defaults.cardCornerPreset)

        val tObj = root.optJSONObject("texts")
        val dt = defaults.texts
        val texts = if (tObj == null) dt else AppTextsConfig(
            brandTitle = tObj.optString("brandTitle", dt.brandTitle),
            brandHindiTitle = tObj.optString("brandHindiTitle", dt.brandHindiTitle),
            searchPlaceholder = tObj.optString("searchPlaceholder", dt.searchPlaceholder),
            deliveryPrefix = tObj.optString("deliveryPrefix", dt.deliveryPrefix),
            announcementText = tObj.optString("announcementText", dt.announcementText),
            saleBadgeText = tObj.optString("saleBadgeText", dt.saleBadgeText),
            saleTimerLabel = tObj.optString("saleTimerLabel", dt.saleTimerLabel),
            slide2Badge = tObj.optString("slide2Badge", dt.slide2Badge),
            slide2Title = tObj.optString("slide2Title", dt.slide2Title),
            slide2Subtitle = tObj.optString("slide2Subtitle", dt.slide2Subtitle),
            slide3Badge = tObj.optString("slide3Badge", dt.slide3Badge),
            slide3Title = tObj.optString("slide3Title", dt.slide3Title),
            slide3Subtitle = tObj.optString("slide3Subtitle", dt.slide3Subtitle),
            devProgressTitle = tObj.optString("devProgressTitle", dt.devProgressTitle),
            devProgressSubtitle = tObj.optString("devProgressSubtitle", dt.devProgressSubtitle),
            devProgressCta = tObj.optString("devProgressCta", dt.devProgressCta),
            dealsHeadingFirst = tObj.optString("dealsHeadingFirst", dt.dealsHeadingFirst),
            dealsHeadingSecond = tObj.optString("dealsHeadingSecond", dt.dealsHeadingSecond),
            tabHomeLabel = tObj.optString("tabHomeLabel", dt.tabHomeLabel),
            tabCategoriesLabel = tObj.optString("tabCategoriesLabel", dt.tabCategoriesLabel),
            tabMallLabel = tObj.optString("tabMallLabel", dt.tabMallLabel),
            tabVideoFindsLabel = tObj.optString("tabVideoFindsLabel", dt.tabVideoFindsLabel),
            tabOrdersLabel = tObj.optString("tabOrdersLabel", dt.tabOrdersLabel),
            addToCartButtonText = tObj.optString("addToCartButtonText", dt.addToCartButtonText),
            footerText = tObj.optString("footerText", dt.footerText)
        )

        val fObj = root.optJSONObject("features")
        val df = defaults.features
        val features = if (fObj == null) df else AppFeaturesConfig(
            showHeroSlider = fObj.optBoolean("showHeroSlider", df.showHeroSlider),
            showAnnouncementTicker = fObj.optBoolean("showAnnouncementTicker", df.showAnnouncementTicker),
            showCountdownBanner = fObj.optBoolean("showCountdownBanner", df.showCountdownBanner),
            showDevProgressStrip = fObj.optBoolean("showDevProgressStrip", df.showDevProgressStrip),
            showCategoryStrip = fObj.optBoolean("showCategoryStrip", df.showCategoryStrip),
            showFilterBar = fObj.optBoolean("showFilterBar", df.showFilterBar),
            showVoiceSearch = fObj.optBoolean("showVoiceSearch", df.showVoiceSearch),
            showCameraSearch = fObj.optBoolean("showCameraSearch", df.showCameraSearch),
            showWishlistHearts = fObj.optBoolean("showWishlistHearts", df.showWishlistHearts),
            showShareButtons = fObj.optBoolean("showShareButtons", df.showShareButtons),
            showAddToCartButtons = fObj.optBoolean("showAddToCartButtons", df.showAddToCartButtons),
            showSaleBadges = fObj.optBoolean("showSaleBadges", df.showSaleBadges),
            showMallBadges = fObj.optBoolean("showMallBadges", df.showMallBadges),
            showCategoriesTab = fObj.optBoolean("showCategoriesTab", df.showCategoriesTab),
            showMallTab = fObj.optBoolean("showMallTab", df.showMallTab),
            showVideoFindsTab = fObj.optBoolean("showVideoFindsTab", df.showVideoFindsTab),
            showOrdersTab = fObj.optBoolean("showOrdersTab", df.showOrdersTab),
            showQuickEditShortcut = fObj.optBoolean("showQuickEditShortcut", df.showQuickEditShortcut),
            showFloatingQuickControls = fObj.optBoolean("showFloatingQuickControls", df.showFloatingQuickControls)
        )

        val iObj = root.optJSONObject("images")
        val di = defaults.images
        val images = if (iObj == null) di else AppImagesConfig(
            brandAvatarCustomUri = iObj.optString("brandAvatarCustomUri", "").ifBlank { null },
            brandAvatarDrawable = iObj.optInt("brandAvatarDrawable", -1).takeIf { it != -1 },
            slide1Enabled = iObj.optBoolean("slide1Enabled", di.slide1Enabled),
            slide1CustomUri = iObj.optString("slide1CustomUri", "").ifBlank { null },
            slide1Drawable = iObj.optInt("slide1Drawable", di.slide1Drawable),
            slide2Enabled = iObj.optBoolean("slide2Enabled", di.slide2Enabled),
            slide2CustomUri = iObj.optString("slide2CustomUri", "").ifBlank { null },
            slide2Drawable = iObj.optInt("slide2Drawable", di.slide2Drawable),
            slide3Enabled = iObj.optBoolean("slide3Enabled", di.slide3Enabled),
            slide3CustomUri = iObj.optString("slide3CustomUri", "").ifBlank { null },
            slide3Drawable = iObj.optInt("slide3Drawable", -1).takeIf { it != -1 }
        )

        val tyObj = root.optJSONObject("typography")
        val dty = defaults.typography
        val typography = if (tyObj == null) dty else AppTypographyConfig(
            fontStyle = runCatching {
                AppFontStyle.valueOf(tyObj.optString("fontStyle", dty.fontStyle.name))
            }.getOrDefault(dty.fontStyle),
            fontScale = tyObj.optDouble("fontScale", dty.fontScale.toDouble()).toFloat(),
            headingWeight = runCatching {
                HeadingWeightStyle.valueOf(tyObj.optString("headingWeight", dty.headingWeight.name))
            }.getOrDefault(dty.headingWeight),
            letterSpacing = runCatching {
                LetterSpacingPreset.valueOf(tyObj.optString("letterSpacing", dty.letterSpacing.name))
            }.getOrDefault(dty.letterSpacing)
        )

        val pgObj = root.optJSONObject("paymentGateways")
        val paymentGateways = if (pgObj == null) {
            defaults.paymentGateways
        } else {
            val arr = pgObj.optJSONArray("gateways")
            val list = mutableListOf<PaymentGatewayLinkItem>()
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val gObj = arr.optJSONObject(i) ?: continue
                    val gwUrl = gObj.optString("url", "")
                    val gwName = gObj.optString("name", "")
                    if (gwUrl.isBlank() || gwName.isBlank()) continue
                    val provider = runCatching {
                        PaymentGatewayProviderPreset.valueOf(
                            gObj.optString("provider", PaymentGatewayProviderPreset.CUSTOM.name)
                        )
                    }.getOrDefault(PaymentGatewayProviderPreset.CUSTOM)
                    list.add(
                        PaymentGatewayLinkItem(
                            id = gObj.optString("id", "gw-$i"),
                            name = gwName,
                            url = gwUrl,
                            provider = provider,
                            isEnabled = gObj.optBoolean("isEnabled", true),
                            lastStatus = gObj.optString("lastStatus", "Connected")
                        )
                    )
                }
            }
            val alreadySeeded = pgObj.optBoolean("hasSeededDefaults", false)
            if (!alreadySeeded && list.isEmpty()) {
                PaymentGatewaySettingsConfig(
                    activeGatewayId = PaymentGatewayDefaults.allGateways.firstOrNull()?.id,
                    gateways = PaymentGatewayDefaults.allGateways,
                    appendOrderParams = pgObj.optBoolean("appendOrderParams", true),
                    hasSeededDefaults = true
                )
            } else {
                PaymentGatewaySettingsConfig(
                    activeGatewayId = pgObj.optString("activeGatewayId", "").ifBlank { null },
                    gateways = list,
                    appendOrderParams = pgObj.optBoolean("appendOrderParams", true),
                    hasSeededDefaults = true
                )
            }
        }

        val saObj = root.optJSONObject("socialAds")
        val socialAds = if (saObj == null) {
            defaults.socialAds
        } else {
            val campaignsArr = saObj.optJSONArray("campaigns")
            val campaignsList = if (campaignsArr == null) {
                SocialMediaAdsDefaults.campaigns
            } else {
                buildList {
                    for (i in 0 until campaignsArr.length()) {
                        val cObj = campaignsArr.optJSONObject(i) ?: continue
                        val title = cObj.optString("title", "")
                        val targetUrl = cObj.optString("targetUrl", "")
                        if (title.isBlank() || targetUrl.isBlank()) continue
                        val platform = runCatching {
                            SocialAdPlatform.valueOf(
                                cObj.optString("platform", SocialAdPlatform.INSTAGRAM.name)
                            )
                        }.getOrDefault(SocialAdPlatform.INSTAGRAM)
                        add(
                            SocialMediaAdCampaign(
                                id = cObj.optString("id", "ad-$i"),
                                title = title,
                                platform = platform,
                                headline = cObj.optString("headline", title),
                                caption = cObj.optString("caption", ""),
                                targetUrl = targetUrl,
                                dailyBudgetInr = cObj.optInt("dailyBudgetInr", 500),
                                impressions = cObj.optInt("impressions", 12000),
                                clicks = cObj.optInt("clicks", 350),
                                ordersGenerated = cObj.optInt("ordersGenerated", 20),
                                isActive = cObj.optBoolean("isActive", true),
                                showOnHomeBanner = cObj.optBoolean("showOnHomeBanner", true),
                                ctaLabel = cObj.optString("ctaLabel", "Shop Deal")
                            )
                        )
                    }
                }
            }
            SocialMediaAdsConfig(
                adsEnabled = saObj.optBoolean("adsEnabled", true),
                showOnHome = saObj.optBoolean("showOnHome", true),
                campaigns = campaignsList
            )
        }

        val wiObj = root.optJSONObject("websiteIntegration")
        val dwi = defaults.websiteIntegration
        val websiteIntegration = if (wiObj == null) dwi else WebsiteIntegrationConfig(
            websiteDomain = wiObj.optString("websiteDomain", dwi.websiteDomain),
            webStoreTitle = wiObj.optString("webStoreTitle", dwi.webStoreTitle),
            webCheckoutSyncEnabled = wiObj.optBoolean("webCheckoutSyncEnabled", dwi.webCheckoutSyncEnabled),
            deepLinkBaseUri = wiObj.optString("deepLinkBaseUri", dwi.deepLinkBaseUri),
            maintenanceMode = wiObj.optBoolean("maintenanceMode", dwi.maintenanceMode),
            termsAndConditions = wiObj.optString("termsAndConditions", dwi.termsAndConditions),
            privacyPolicy = wiObj.optString("privacyPolicy", dwi.privacyPolicy)
        )

        val cmObj = root.optJSONObject("commissions")
        val dcm = defaults.commissions
        val commissions = if (cmObj == null) dcm else CommissionConfig(
            globalPercent = cmObj.optDouble("globalPercent", dcm.globalPercent.toDouble()).toFloat(),
            sareePercent = cmObj.optDouble("sareePercent", dcm.sareePercent.toDouble()).toFloat(),
            kurtiPercent = cmObj.optDouble("kurtiPercent", dcm.kurtiPercent.toDouble()).toFloat(),
            lehengaPercent = cmObj.optDouble("lehengaPercent", dcm.lehengaPercent.toDouble()).toFloat(),
            kidsPercent = cmObj.optDouble("kidsPercent", dcm.kidsPercent.toDouble()).toFloat()
        )

        return AppManagementConfig(
            themePreset = themePreset,
            homeLayoutMode = homeLayout,
            cardCornerPreset = cardCorner,
            texts = texts,
            features = features,
            images = images,
            typography = typography,
            paymentGateways = paymentGateways,
            socialAds = socialAds,
            websiteIntegration = websiteIntegration,
            commissions = commissions
        )
    }

    fun saveBackupPayload(
        snapshot: AppBackupSnapshot,
        config: AppManagementConfig,
        categories: List<CategoryItem>,
        productImageOverrides: Map<String, String>,
        wishlistIds: Set<String>,
        deliveryAddress: String
    ): String {
        val backupJson = JSONObject().apply {
            put("id", snapshot.id)
            put("label", snapshot.label)
            put("createdAt", snapshot.createdAt)
            put("summary", snapshot.summary)
            put("themeName", snapshot.themeName)
            put("fontName", snapshot.fontName)
            put("productCount", snapshot.productCount)
            put("categoryCount", snapshot.categoryCount)
            put("config", JSONObject(encodeConfig(config)))
            put("deliveryAddress", deliveryAddress)
            put("wishlistIds", JSONArray().apply { wishlistIds.forEach { put(it) } })
            put("categories", JSONArray().apply {
                categories.forEach { cat ->
                    put(JSONObject().apply {
                        put("id", cat.id)
                        put("name", cat.name)
                        put("imageRes", cat.imageRes)
                        put("offerBadge", cat.offerBadge)
                        put("customImageUri", cat.customImageUri ?: "")
                    })
                }
            })
            put("productImageOverrides", JSONObject().apply {
                productImageOverrides.forEach { (k, v) -> put(k, v) }
            })
        }

        val currentArr = runCatching {
            JSONArray(prefs.getString(KEY_BACKUPS_ARRAY_JSON, "[]") ?: "[]")
        }.getOrDefault(JSONArray())
        val updatedArr = JSONArray()
        updatedArr.put(backupJson)
        for (i in 0 until currentArr.length().coerceAtMost(9)) {
            val item = currentArr.optJSONObject(i)
            if (item != null && item.optString("id") != snapshot.id) {
                updatedArr.put(item)
            }
        }
        prefs.edit().putString(KEY_BACKUPS_ARRAY_JSON, updatedArr.toString()).apply()
        return backupJson.toString(2)
    }

    fun loadBackupSnapshots(): List<AppBackupSnapshot> {
        val raw = prefs.getString(KEY_BACKUPS_ARRAY_JSON, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<AppBackupSnapshot>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AppBackupSnapshot(
                        id = obj.getString("id"),
                        label = obj.optString("label", "App Backup"),
                        createdAt = obj.optString("createdAt", ""),
                        summary = obj.optString("summary", ""),
                        themeName = obj.optString("themeName", ""),
                        fontName = obj.optString("fontName", ""),
                        productCount = obj.optInt("productCount", 0),
                        categoryCount = obj.optInt("categoryCount", 0)
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    data class RestoredBackupBundle(
        val snapshot: AppBackupSnapshot,
        val config: AppManagementConfig,
        val categories: List<CategoryItem>,
        val productImageOverrides: Map<String, String>,
        val wishlistIds: Set<String>,
        val deliveryAddress: String,
        val rawJson: String
    )

    fun loadBackupBundle(backupId: String): RestoredBackupBundle? {
        val raw = prefs.getString(KEY_BACKUPS_ARRAY_JSON, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("id") == backupId) {
                    val snapshot = AppBackupSnapshot(
                        id = obj.getString("id"),
                        label = obj.optString("label", "App Backup"),
                        createdAt = obj.optString("createdAt", ""),
                        summary = obj.optString("summary", ""),
                        themeName = obj.optString("themeName", ""),
                        fontName = obj.optString("fontName", ""),
                        productCount = obj.optInt("productCount", 0),
                        categoryCount = obj.optInt("categoryCount", 0)
                    )
                    val cfg = obj.optJSONObject("config")?.let { decodeConfig(it.toString()) } ?: AppManagementConfig()
                    val catArr = obj.optJSONArray("categories")
                    val cats = mutableListOf<CategoryItem>()
                    if (catArr != null) {
                        for (cIdx in 0 until catArr.length()) {
                            val cObj = catArr.getJSONObject(cIdx)
                            cats.add(
                                CategoryItem(
                                    id = cObj.getString("id"),
                                    name = cObj.getString("name"),
                                    imageRes = cObj.optInt("imageRes", R.drawable.dress_anarkali_1793417013639),
                                    offerBadge = cObj.optString("offerBadge", ""),
                                    customImageUri = cObj.optString("customImageUri", "").ifBlank { null }
                                )
                            )
                        }
                    }
                    val imgObj = obj.optJSONObject("productImageOverrides")
                    val imgMap = mutableMapOf<String, String>()
                    imgObj?.keys()?.forEach { k ->
                        val v = imgObj.optString(k, "")
                        if (v.isNotBlank()) imgMap[k] = v
                    }
                    val wishArr = obj.optJSONArray("wishlistIds")
                    val wishSet = mutableSetOf<String>()
                    if (wishArr != null) {
                        for (wIdx in 0 until wishArr.length()) {
                            wishSet.add(wishArr.getString(wIdx))
                        }
                    }
                    return RestoredBackupBundle(
                        snapshot = snapshot,
                        config = cfg,
                        categories = cats,
                        productImageOverrides = imgMap,
                        wishlistIds = wishSet,
                        deliveryAddress = obj.optString("deliveryAddress", "Bettiah - 845438"),
                        rawJson = obj.toString(2)
                    )
                }
            }
            null
        }.getOrNull()
    }

    fun deleteBackup(backupId: String) {
        val raw = prefs.getString(KEY_BACKUPS_ARRAY_JSON, null) ?: return
        runCatching {
            val arr = JSONArray(raw)
            val updated = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("id") != backupId) {
                    updated.put(obj)
                }
            }
            prefs.edit().putString(KEY_BACKUPS_ARRAY_JSON, updated.toString()).apply()
        }
    }

    companion object {
        private const val KEY_CONFIG_JSON = "app_management_config_json"
        private const val KEY_CATEGORIES_JSON = "app_management_categories_json"
        private const val KEY_SUBCATEGORIES_JSON = "app_management_subcategories_json"
        private const val KEY_PRODUCT_IMAGES_JSON = "app_management_product_images_json"
        private const val KEY_BACKUPS_ARRAY_JSON = "app_management_backups_json"
    }
}
