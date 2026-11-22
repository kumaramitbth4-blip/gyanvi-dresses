package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppFontStyle
import com.example.model.AppTypographyConfig
import com.example.model.HeadingWeightStyle

// Custom Google Fonts installed locally via font-util
val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.poppins, FontWeight.Medium),
    Font(R.font.poppins, FontWeight.Bold)
)

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold)
)

val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val PacificoFontFamily = FontFamily(
    Font(R.font.pacifico, FontWeight.Normal)
)

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val LoraFontFamily = FontFamily(
    Font(R.font.lora, FontWeight.Normal),
    Font(R.font.lora, FontWeight.Medium),
    Font(R.font.lora, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

fun displayFontFamilyFor(style: AppFontStyle): FontFamily = when (style) {
    AppFontStyle.MODERN_BOUTIQUE -> OutfitFontFamily
    AppFontStyle.PLAYFAIR_SERIF -> PlayfairDisplayFontFamily
    AppFontStyle.PACIFICO_CURSIVE -> PacificoFontFamily
    AppFontStyle.SPACE_GROTESK -> SpaceGroteskFontFamily
    AppFontStyle.LORA_EDITORIAL -> LoraFontFamily
    AppFontStyle.JETBRAINS_MONO -> JetBrainsMonoFontFamily
}

fun bodyFontFamilyFor(style: AppFontStyle): FontFamily = when (style) {
    AppFontStyle.MODERN_BOUTIQUE -> PoppinsFontFamily
    AppFontStyle.PLAYFAIR_SERIF -> LoraFontFamily
    AppFontStyle.PACIFICO_CURSIVE -> PoppinsFontFamily
    AppFontStyle.SPACE_GROTESK -> SpaceGroteskFontFamily
    AppFontStyle.LORA_EDITORIAL -> LoraFontFamily
    AppFontStyle.JETBRAINS_MONO -> JetBrainsMonoFontFamily
}

fun buildGyanviTypography(config: AppTypographyConfig): Typography {
    val displayFamily = displayFontFamilyFor(config.fontStyle)
    val bodyFamily = bodyFontFamilyFor(config.fontStyle)
    val scale = config.fontScale.coerceIn(0.85f, 1.30f)
    val extraSpacing = config.letterSpacing.extraSp
    val headingWeight = when (config.headingWeight) {
        HeadingWeightStyle.NORMAL -> FontWeight.Medium
        HeadingWeightStyle.BOLD -> FontWeight.Bold
        HeadingWeightStyle.EXTRA_BOLD -> FontWeight.ExtraBold
    }
    val subWeight = when (config.headingWeight) {
        HeadingWeightStyle.NORMAL -> FontWeight.Normal
        HeadingWeightStyle.BOLD -> FontWeight.SemiBold
        HeadingWeightStyle.EXTRA_BOLD -> FontWeight.Bold
    }

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = headingWeight,
            fontSize = (30f * scale).sp,
            lineHeight = (36f * scale).sp,
            letterSpacing = extraSpacing.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = headingWeight,
            fontSize = (24f * scale).sp,
            lineHeight = (30f * scale).sp,
            letterSpacing = extraSpacing.sp
        ),
        titleLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = headingWeight,
            fontSize = (19f * scale).sp,
            lineHeight = (25f * scale).sp,
            letterSpacing = (0.15f + extraSpacing).sp
        ),
        titleMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = subWeight,
            fontSize = (16f * scale).sp,
            lineHeight = (22f * scale).sp,
            letterSpacing = (0.15f + extraSpacing).sp
        ),
        titleSmall = TextStyle(
            fontFamily = displayFamily,
            fontWeight = subWeight,
            fontSize = (14f * scale).sp,
            lineHeight = (20f * scale).sp,
            letterSpacing = (0.1f + extraSpacing).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15f * scale).sp,
            lineHeight = (22f * scale).sp,
            letterSpacing = (0.25f + extraSpacing).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13f * scale).sp,
            lineHeight = (18f * scale).sp,
            letterSpacing = (0.25f + extraSpacing).sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (11f * scale).sp,
            lineHeight = (15f * scale).sp,
            letterSpacing = (0.3f + extraSpacing).sp
        ),
        labelLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = subWeight,
            fontSize = (14f * scale).sp,
            lineHeight = (20f * scale).sp,
            letterSpacing = (0.1f + extraSpacing).sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12f * scale).sp,
            lineHeight = (16f * scale).sp,
            letterSpacing = (0.2f + extraSpacing).sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (10f * scale).sp,
            lineHeight = (14f * scale).sp,
            letterSpacing = (0.2f + extraSpacing).sp
        )
    )
}

val GyanviTypography = buildGyanviTypography(AppTypographyConfig())
