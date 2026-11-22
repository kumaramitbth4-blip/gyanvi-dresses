package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R

enum class DashboardThemePreset(
    val displayName: String,
    val hindiLabel: String,
    val backgroundColor: Color,
    val primaryColor: Color,
    val accentColor: Color,
    val containerColor: Color
) {
    YELLOW_PINK(
        displayName = "Light Yellow & Pink",
        hindiLabel = "पीला और गुलाबी (डिफ़ॉल्ट)",
        backgroundColor = Color(0xFFFFFBE8),
        primaryColor = Color(0xFFFFB300),
        accentColor = Color(0xFF9C1B68),
        containerColor = Color(0xFFFFECB3)
    ),
    ROYAL_MAROON_GOLD(
        displayName = "Royal Maroon & Gold",
        hindiLabel = "शाही मैरून और गोल्ड",
        backgroundColor = Color(0xFFFFF7ED),
        primaryColor = Color(0xFFD97706),
        accentColor = Color(0xFF881337),
        containerColor = Color(0xFFFDE68A)
    ),
    EMERALD_IVORY(
        displayName = "Emerald & Ivory",
        hindiLabel = "हरा पन्ना थीम",
        backgroundColor = Color(0xFFF0FDF4),
        primaryColor = Color(0xFF10B981),
        accentColor = Color(0xFF065F46),
        containerColor = Color(0xFFD1FAE5)
    ),
    SAPPHIRE_ROSE(
        displayName = "Sapphire & Rose",
        hindiLabel = "नीला और रोज़ थीम",
        backgroundColor = Color(0xFFEFF6FF),
        primaryColor = Color(0xFF3B82F6),
        accentColor = Color(0xFFBE185D),
        containerColor = Color(0xFFDBEAFE)
    ),
    LAVENDER_PLUM(
        displayName = "Lavender & Plum",
        hindiLabel = "लैवेंडर बुटीक थीम",
        backgroundColor = Color(0xFFFAF5FF),
        primaryColor = Color(0xFF8B5CF6),
        accentColor = Color(0xFF701A75),
        containerColor = Color(0xFFEDE9FE)
    )
}

enum class AppFontStyle(
    val displayName: String,
    val subtitle: String,
    val sampleText: String
) {
    MODERN_BOUTIQUE(
        displayName = "Modern Boutique",
        subtitle = "Outfit + Poppins (Default)",
        sampleText = "Gyanvi Dresses • ज्ञानवी ड्रेसेस"
    ),
    PLAYFAIR_SERIF(
        displayName = "Royal Heritage Serif",
        subtitle = "Playfair Display • शाही लिखावट",
        sampleText = "Royal Bridal Collection"
    ),
    PACIFICO_CURSIVE(
        displayName = "Stylist Cursive",
        subtitle = "Pacifico • हस्तलिखित स्टाइल",
        sampleText = "Gyanvi Boutique"
    ),
    SPACE_GROTESK(
        displayName = "Bold Geometric",
        subtitle = "Space Grotesk • मॉडर्न बोल्ड",
        sampleText = "MAHA DHAMAKA SALE 80%"
    ),
    LORA_EDITORIAL(
        displayName = "Classic Editorial",
        subtitle = "Lora Serif • एलीगेंट लिखावट",
        sampleText = "Banarasi Silk Sarees"
    ),
    JETBRAINS_MONO(
        displayName = "Digital Monospace",
        subtitle = "JetBrains Mono • साफ़ डिजिटल",
        sampleText = "SKU #GD-2026 • ₹599"
    )
}

enum class HeadingWeightStyle(val label: String) {
    NORMAL("Regular"),
    BOLD("Bold"),
    EXTRA_BOLD("Extra Heavy")
}

enum class LetterSpacingPreset(val label: String, val extraSp: Float) {
    TIGHT("Tight", -0.3f),
    NORMAL("Normal", 0.0f),
    WIDE("Wide", 0.8f)
}

enum class HomeLayoutMode(val label: String, val columns: Int) {
    TWO_COLUMN("2 Columns (Standard)", 2),
    THREE_COLUMN("3 Columns (Compact)", 3),
    SINGLE_LARGE("1 Column (Showcase)", 1)
}

enum class CardCornerPreset(val label: String, val radiusDp: Int) {
    SHARP("Sharp (6dp)", 6),
    BALANCED("Balanced (12dp)", 12),
    ROUNDED("Rounded (20dp)", 20)
}

data class AppTextsConfig(
    val brandTitle: String = "Gyanvi Dresses Boutique",
    val brandHindiTitle: String = "ज्ञानवी ड्रेसेस मार्केटप्लेस",
    val searchPlaceholder: String = "Search by Keyword or Product ID",
    val deliveryPrefix: String = "Delivering to",
    val announcementText: String = "",
    val saleBadgeText: String = "UP TO 80% OFF",
    val saleTimerLabel: String = "Sale Ends in",
    val slide2Badge: String = "WEDDING & FESTIVE SLIDER",
    val slide2Title: String = "Bridal Lehengas & Banarasi Sarees",
    val slide2Subtitle: String = "Use Code FESTIVE15 • Free Shiprocket Delivery",
    val slide3Badge: String = "GYANVI MULTI-VENDOR MARKETPLACE",
    val slide3Title: String = "Buyer • Seller • Admin All-In-One",
    val slide3Subtitle: String = "Tap to check Weekly Progress & Setup 15 Modules →",
    val devProgressTitle: String = "साप्ताहिक Development Progress चेक करें",
    val devProgressSubtitle: String = "15 Marketplace Modules Ready • Buyer, Seller & Admin",
    val devProgressCta: String = "Tap to Set Up",
    val dealsHeadingFirst: String = "BEST",
    val dealsHeadingSecond: String = "PRICE DEALS",
    val tabHomeLabel: String = "Home",
    val tabCategoriesLabel: String = "Categories",
    val tabMallLabel: String = "Mall",
    val tabVideoFindsLabel: String = "Video Finds",
    val tabOrdersLabel: String = "My Orders",
    val addToCartButtonText: String = "Add to Cart",
    val footerText: String = "Made with ♥ for Bharat • Gyanvi Dresses"
)

data class AppFeaturesConfig(
    val showHeroSlider: Boolean = true,
    val showAnnouncementTicker: Boolean = true,
    val showCountdownBanner: Boolean = true,
    val showDevProgressStrip: Boolean = true,
    val showCategoryStrip: Boolean = true,
    val showFilterBar: Boolean = true,
    val showVoiceSearch: Boolean = true,
    val showCameraSearch: Boolean = true,
    val showWishlistHearts: Boolean = true,
    val showShareButtons: Boolean = true,
    val showAddToCartButtons: Boolean = true,
    val showSaleBadges: Boolean = true,
    val showMallBadges: Boolean = true,
    val showCategoriesTab: Boolean = true,
    val showMallTab: Boolean = true,
    val showVideoFindsTab: Boolean = true,
    val showOrdersTab: Boolean = true,
    val showQuickEditShortcut: Boolean = true,
    val showFloatingQuickControls: Boolean = true
)

data class AppBackupSnapshot(
    val id: String,
    val label: String,
    val createdAt: String,
    val summary: String,
    val themeName: String,
    val fontName: String,
    val productCount: Int,
    val categoryCount: Int
)

data class AppImagesConfig(
    val brandAvatarCustomUri: String? = null,
    val brandAvatarDrawable: Int? = R.drawable.gyanvi_brand_logo,
    val slide1Enabled: Boolean = true,
    val slide1CustomUri: String? = null,
    val slide1Drawable: Int = R.drawable.gyanvi_hero_banner_1793416998706,
    val slide2Enabled: Boolean = true,
    val slide2CustomUri: String? = null,
    val slide2Drawable: Int = R.drawable.dress_lehenga_1793417067159,
    val slide3Enabled: Boolean = true,
    val slide3CustomUri: String? = null,
    val slide3Drawable: Int? = null
)

data class AppTypographyConfig(
    val fontStyle: AppFontStyle = AppFontStyle.MODERN_BOUTIQUE,
    val fontScale: Float = 1.0f,
    val headingWeight: HeadingWeightStyle = HeadingWeightStyle.BOLD,
    val letterSpacing: LetterSpacingPreset = LetterSpacingPreset.NORMAL
)

enum class PaymentGatewayProviderPreset(val displayName: String, val hintUrl: String) {
    RAZORPAY("Razorpay Payment Link", "https://rzp.io/l/gyanvi-dresses"),
    PHONEPE("PhonePe Payment Gateway", "https://mercury.phonepe.com/transact/gyanvi-dresses"),
    PAYTM("Paytm Payment Gateway", "https://paytm.me/gyanvi-dresses"),
    CASHFREE("Cashfree Payment Link", "https://payments.cashfree.com/links/gyanvi-dresses"),
    GPAY_UPI("Google Pay Business", "https://pay.google.com/intl/en_in/about/business/"),
    PAYU("PayU Payment Link", "https://pmny.in/gyanvi-dresses"),
    INSTAMOJO("Instamojo Payment Link", "https://imjo.in/gyanvi-dresses"),
    CCAVENUE("CCAvenue Checkout", "https://secure.ccavenue.com/transaction/gyanvi"),
    EASEBUZZ("Easebuzz Payment Link", "https://pay.easebuzz.in/easycollect/gyanvi-dresses"),
    BILLDESK("BillDesk Gateway", "https://pgi.billdesk.com/pgidsk/PGIMerchantPayment"),
    STRIPE("Stripe Checkout Link", "https://buy.stripe.com/gyanvi-dresses"),
    PAYPAL("PayPal Checkout", "https://www.paypal.com/paypalme/gyanvidresses"),
    CUSTOM("Custom Payment URL", "https://pay.yourdomain.com/checkout")
}

data class PaymentGatewayLinkItem(
    val id: String,
    val name: String,
    val url: String,
    val provider: PaymentGatewayProviderPreset = PaymentGatewayProviderPreset.CUSTOM,
    val isEnabled: Boolean = true,
    val lastStatus: String = "Connected"
)

object PaymentGatewayDefaults {
    val allGateways: List<PaymentGatewayLinkItem> = listOf(
        PaymentGatewayLinkItem(
            id = "pg-razorpay",
            name = "Razorpay",
            url = "https://rzp.io/l/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.RAZORPAY,
            isEnabled = true,
            lastStatus = "Ready (UPI / Cards / NetBanking)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-phonepe",
            name = "PhonePe PG",
            url = "https://mercury.phonepe.com/transact/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.PHONEPE,
            isEnabled = true,
            lastStatus = "Ready (PhonePe / UPI)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-paytm",
            name = "Paytm PG",
            url = "https://paytm.me/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.PAYTM,
            isEnabled = true,
            lastStatus = "Ready (Paytm Wallet / UPI)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-cashfree",
            name = "Cashfree",
            url = "https://payments.cashfree.com/links/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.CASHFREE,
            isEnabled = true,
            lastStatus = "Ready (Instant Settlement)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-gpay",
            name = "Google Pay Business",
            url = "https://pay.google.com/intl/en_in/about/business/",
            provider = PaymentGatewayProviderPreset.GPAY_UPI,
            isEnabled = true,
            lastStatus = "Ready (GPay / UPI)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-payu",
            name = "PayU",
            url = "https://pmny.in/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.PAYU,
            isEnabled = true,
            lastStatus = "Ready (Cards / EMI / UPI)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-instamojo",
            name = "Instamojo",
            url = "https://imjo.in/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.INSTAMOJO,
            isEnabled = true,
            lastStatus = "Ready (Payment Link)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-ccavenue",
            name = "CCAvenue",
            url = "https://secure.ccavenue.com/transaction/gyanvi",
            provider = PaymentGatewayProviderPreset.CCAVENUE,
            isEnabled = true,
            lastStatus = "Ready (200+ Payment Options)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-easebuzz",
            name = "Easebuzz",
            url = "https://pay.easebuzz.in/easycollect/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.EASEBUZZ,
            isEnabled = true,
            lastStatus = "Ready (EasyCollect Link)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-billdesk",
            name = "BillDesk",
            url = "https://pgi.billdesk.com/pgidsk/PGIMerchantPayment",
            provider = PaymentGatewayProviderPreset.BILLDESK,
            isEnabled = true,
            lastStatus = "Ready (NetBanking / Cards)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-stripe",
            name = "Stripe",
            url = "https://buy.stripe.com/gyanvi-dresses",
            provider = PaymentGatewayProviderPreset.STRIPE,
            isEnabled = true,
            lastStatus = "Ready (International Cards)"
        ),
        PaymentGatewayLinkItem(
            id = "pg-paypal",
            name = "PayPal",
            url = "https://www.paypal.com/paypalme/gyanvidresses",
            provider = PaymentGatewayProviderPreset.PAYPAL,
            isEnabled = true,
            lastStatus = "Ready (Global Checkout)"
        )
    )
}

data class PaymentGatewaySettingsConfig(
    val activeGatewayId: String? = "pg-razorpay",
    val gateways: List<PaymentGatewayLinkItem> = PaymentGatewayDefaults.allGateways,
    val appendOrderParams: Boolean = true,
    val hasSeededDefaults: Boolean = true
) {
    val activeGateway: PaymentGatewayLinkItem?
        get() = gateways.firstOrNull { it.id == activeGatewayId && it.isEnabled }
}

enum class SocialAdPlatform(
    val displayName: String,
    val badgeLabel: String,
    val accentHex: Long,
    val defaultUrl: String
) {
    INSTAGRAM("Instagram Reels & Stories", "Instagram", 0xFFE1306C, "https://www.instagram.com/"),
    FACEBOOK("Facebook Marketplace Ads", "Facebook", 0xFF1877F2, "https://www.facebook.com/"),
    YOUTUBE("YouTube Shorts & Shopping", "YouTube", 0xFFFF0000, "https://www.youtube.com/"),
    WHATSAPP("WhatsApp Business Ads", "WhatsApp", 0xFF25D366, "https://wa.me/"),
    GOOGLE_ADS("Google Shopping Ads", "Google Ads", 0xFF4285F4, "https://ads.google.com/"),
    SNAPCHAT("Snapchat Spotlight Ads", "Snapchat", 0xFFF59E0B, "https://forbusiness.snapchat.com/")
}

data class SocialMediaAdCampaign(
    val id: String,
    val title: String,
    val platform: SocialAdPlatform,
    val headline: String,
    val caption: String,
    val targetUrl: String,
    val dailyBudgetInr: Int = 500,
    val impressions: Int = 12400,
    val clicks: Int = 480,
    val ordersGenerated: Int = 34,
    val isActive: Boolean = true,
    val showOnHomeBanner: Boolean = true,
    val ctaLabel: String = "Shop Deal"
)

object SocialMediaAdsDefaults {
    val campaigns: List<SocialMediaAdCampaign> = listOf(
        SocialMediaAdCampaign(
            id = "ad-insta-festive",
            title = "Festive Bridal Lehenga Reel Ad",
            platform = SocialAdPlatform.INSTAGRAM,
            headline = "Bridal Lehengas Starting ₹999 ✨",
            caption = "Trending Instagram Reel Offer • Extra 15% Off Code: FESTIVE15",
            targetUrl = "https://www.instagram.com/",
            dailyBudgetInr = 750,
            impressions = 48200,
            clicks = 2190,
            ordersGenerated = 148,
            isActive = true,
            showOnHomeBanner = true,
            ctaLabel = "View Reel Ad"
        ),
        SocialMediaAdCampaign(
            id = "ad-fb-saree",
            title = "Banarasi Silk Saree Carousel Ad",
            platform = SocialAdPlatform.FACEBOOK,
            headline = "Pure Banarasi Silk Sarees • Free Delivery",
            caption = "Facebook Marketplace Sponsored Collection • Direct Boutique Rates",
            targetUrl = "https://www.facebook.com/",
            dailyBudgetInr = 600,
            impressions = 35900,
            clicks = 1420,
            ordersGenerated = 96,
            isActive = true,
            showOnHomeBanner = true,
            ctaLabel = "Open Ad"
        ),
        SocialMediaAdCampaign(
            id = "ad-yt-kurti",
            title = "YouTube Shorts Ethnic Kurti Haul",
            platform = SocialAdPlatform.YOUTUBE,
            headline = "Designer Anarkali Kurtis Under ₹499",
            caption = "Watch Festive Try-On Haul on YouTube Shorts & Grab Flash Deals",
            targetUrl = "https://www.youtube.com/",
            dailyBudgetInr = 500,
            impressions = 29400,
            clicks = 1150,
            ordersGenerated = 72,
            isActive = true,
            showOnHomeBanner = true,
            ctaLabel = "Watch Short"
        ),
        SocialMediaAdCampaign(
            id = "ad-wa-broadcast",
            title = "WhatsApp VIP Wedding Catalog",
            platform = SocialAdPlatform.WHATSAPP,
            headline = "Wholesale & Bridal Catalog on WhatsApp",
            caption = "Instant WhatsApp Business Broadcast Deals for Gyanvi VIP Buyers",
            targetUrl = "https://wa.me/",
            dailyBudgetInr = 350,
            impressions = 18600,
            clicks = 980,
            ordersGenerated = 84,
            isActive = true,
            showOnHomeBanner = true,
            ctaLabel = "Chat & Buy"
        )
    )
}

data class SocialMediaAdsConfig(
    val adsEnabled: Boolean = true,
    val showOnHome: Boolean = true,
    val campaigns: List<SocialMediaAdCampaign> = SocialMediaAdsDefaults.campaigns
)

data class WebsiteIntegrationConfig(
    val websiteDomain: String = "https://www.gyanvidresses.com",
    val webStoreTitle: String = "GYANVI DRESSES Official Web & Mobile Store",
    val webCheckoutSyncEnabled: Boolean = true,
    val deepLinkBaseUri: String = "gyanvi://shop",
    val maintenanceMode: Boolean = false,
    val termsAndConditions: String = "1. Genuine Ethnic & Western Products.\n2. 7-Day Easy Return & Exchange Policy.\n3. COD available up to ₹15,000 across 28,000+ pincodes.\n4. Instant UPI/Bank refund within 24-48 hours after quality check.",
    val privacyPolicy: String = "We collect name, phone, delivery address & encrypted payment telemetry strictly for order fulfillment and live courier tracking. Gyanvi Dresses never sells customer data to third parties."
)

data class CommissionConfig(
    val globalPercent: Float = 8.5f,
    val sareePercent: Float = 7.0f,
    val kurtiPercent: Float = 9.0f,
    val lehengaPercent: Float = 10.0f,
    val kidsPercent: Float = 6.5f
)

data class AppManagementConfig(
    val themePreset: DashboardThemePreset = DashboardThemePreset.YELLOW_PINK,
    val homeLayoutMode: HomeLayoutMode = HomeLayoutMode.TWO_COLUMN,
    val cardCornerPreset: CardCornerPreset = CardCornerPreset.BALANCED,
    val texts: AppTextsConfig = AppTextsConfig(),
    val features: AppFeaturesConfig = AppFeaturesConfig(),
    val images: AppImagesConfig = AppImagesConfig(),
    val typography: AppTypographyConfig = AppTypographyConfig(),
    val paymentGateways: PaymentGatewaySettingsConfig = PaymentGatewaySettingsConfig(),
    val socialAds: SocialMediaAdsConfig = SocialMediaAdsConfig(),
    val websiteIntegration: WebsiteIntegrationConfig = WebsiteIntegrationConfig(),
    val commissions: CommissionConfig = CommissionConfig()
)

data class DrawablePresetOption(
    val label: String,
    val drawableRes: Int
)

object AppDrawablePresets {
    val all = listOf(
        DrawablePresetOption("Maha Sale Banner", R.drawable.gyanvi_hero_banner_1793416998706),
        DrawablePresetOption("Anarkali Kurti", R.drawable.dress_anarkali_1793417013639),
        DrawablePresetOption("Banarasi Saree", R.drawable.dress_saree_1793417052582),
        DrawablePresetOption("Bridal Lehenga", R.drawable.dress_lehenga_1793417067159),
        DrawablePresetOption("Gyanvi Official Logo", R.drawable.gyanvi_brand_logo),
        DrawablePresetOption("Gyanvi Classic Icon", R.drawable.gyanvi_icon_1793416900246),
        DrawablePresetOption("Fashion Banner", R.drawable.search_banner_illustration_1793417120669)
    )
}
