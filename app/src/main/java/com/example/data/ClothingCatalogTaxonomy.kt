package com.example.data

import androidx.annotation.DrawableRes
import com.example.R

data class ClothingSubCategorySpec(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    val keywords: List<String> = emptyList()
)

data class ClothingCategorySpec(
    val id: String,
    val linkedMainCategoryName: String,
    val subcategories: List<ClothingSubCategorySpec>
)

data class ClothingCatalogSearchEntry(
    val linkedMainCategoryName: String,
    val subcategoryName: String,
    val keywords: List<String>,
    @DrawableRes val imageRes: Int
)

object ClothingCatalogTaxonomy {
    val colorOptions: List<String> = listOf(
        "Magenta Pink", "Royal Blue", "Emerald Green", "Maroon", "Haldi Yellow",
        "Crimson Red", "Peacock Teal", "Pastel Lavender", "Ivory Cream", "Jet Black",
        "Gold Zari", "Mint Green", "Wine Purple", "Peach Coral"
    )

    val fabricOptions: List<String> = listOf(
        "Pure Cotton", "Banarasi Silk", "Georgette", "Net Embroidered", "Rayon Slub",
        "Chiffon", "Organza Silk", "Velvet", "Art Silk", "Chanderi Cotton", "Denim"
    )

    val standardSizes: List<String> = listOf(
        "S", "M", "L", "XL", "XXL", "XXXL", "4XL", "5XL"
    )

    val numericSizes: List<String> = listOf(
        "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36", "38", "40", "42"
    )

    val footwearSizes: List<String> = listOf(
        "UK 4", "UK 5", "UK 6", "UK 7", "UK 8", "UK 9", "UK 10", "UK 11"
    )

    val clothingCategories: List<ClothingCategorySpec> = listOf(
        ClothingCategorySpec(
            id = "CAT-WMN-02",
            linkedMainCategoryName = "Women",
            subcategories = listOf(
                ClothingSubCategorySpec(
                    "SUB-TAX-WMN-01",
                    "Sarees",
                    R.drawable.dress_saree_1793417052582,
                    keywords = listOf("Banarasi Saree", "Silk Saree", "Kanjivaram", "Organza Saree", "साड़ी", "बनारसी साड़ी")
                ),
                ClothingSubCategorySpec(
                    "SUB-TAX-WMN-02",
                    "Kurtis & Suits",
                    R.drawable.dress_anarkali_1793417013639,
                    keywords = listOf("Anarkali", "Chikankari Kurti", "Cotton Kurti", "Palazzo Suit", "कुर्ती")
                )
            )
        ),
        ClothingCategorySpec(
            id = "CAT-MEN-01",
            linkedMainCategoryName = "Men",
            subcategories = listOf(
                ClothingSubCategorySpec(
                    "SUB-TAX-MEN-01",
                    "Kurta Pajama",
                    R.drawable.dress_anarkali_1793417013639,
                    keywords = listOf("Men Kurta", "Festive Kurta", "Sherwani", "कुर्ता पजामा")
                ),
                ClothingSubCategorySpec(
                    "SUB-TAX-MEN-02",
                    "Shirts & T-Shirts",
                    R.drawable.dress_anarkali_1793417013639,
                    keywords = listOf("Formal Shirt", "Casual T-Shirt", "Men Denim")
                )
            )
        ),
        ClothingCategorySpec(
            id = "CAT-WST-08",
            linkedMainCategoryName = "Western Wear",
            subcategories = listOf(
                ClothingSubCategorySpec(
                    "SUB-TAX-WST-01",
                    "Gowns & Maxi Dresses",
                    R.drawable.dress_anarkali_1793417013639,
                    keywords = listOf("Maxi Dress", "Party Gown", "Floral Dress", "वेस्टर्न ड्रेस")
                )
            )
        ),
        ClothingCategorySpec(
            id = "CAT-ETH-09",
            linkedMainCategoryName = "Ethnic Wear",
            subcategories = listOf(
                ClothingSubCategorySpec(
                    "SUB-TAX-ETH-01",
                    "Lehenga Choli",
                    R.drawable.dress_lehenga_1793417067159,
                    keywords = listOf("Bridal Lehenga", "Partywear Lehenga", "लहंगा")
                )
            )
        ),
        ClothingCategorySpec(
            id = "CAT-GEW-23",
            linkedMainCategoryName = "Girls Ethnic Wear",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-GEW-01", "Gowns & Sharara", R.drawable.dress_lehenga_1793417067159),
                ClothingSubCategorySpec("SUB-GEW-02", "Kids Lehenga Choli", R.drawable.dress_lehenga_1793417067159),
                ClothingSubCategorySpec("SUB-GEW-03", "Festive Anarkali", R.drawable.dress_anarkali_1793417013639)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-GWW-24",
            linkedMainCategoryName = "Girls Western Wear",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-GWW-01", "Party Frocks & Dresses", R.drawable.dress_anarkali_1793417013639),
                ClothingSubCategorySpec("SUB-GWW-02", "Tops & Denim Sets", R.drawable.dress_anarkali_1793417013639)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-WEW-25",
            linkedMainCategoryName = "Women's Ethnic Wear",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-WEW-01", "Designer Sarees", R.drawable.dress_saree_1793417052582),
                ClothingSubCategorySpec("SUB-WEW-02", "Anarkali Suits", R.drawable.dress_anarkali_1793417013639),
                ClothingSubCategorySpec("SUB-WEW-03", "Kurti Palazzo Sets", R.drawable.dress_anarkali_1793417013639)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-WED-26",
            linkedMainCategoryName = "Wedding & Festive Wear",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-WED-01", "Bridal & Wedding", R.drawable.dress_lehenga_1793417067159),
                ClothingSubCategorySpec("SUB-WED-02", "Partywear Lehenga", R.drawable.dress_lehenga_1793417067159),
                ClothingSubCategorySpec("SUB-WED-03", "Banarasi Silk", R.drawable.dress_saree_1793417052582)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-BCL-27",
            linkedMainCategoryName = "Boys Clothing",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-BCL-01", "Boys Kurta Sets", R.drawable.dress_anarkali_1793417013639),
                ClothingSubCategorySpec("SUB-BCL-02", "Boys Casual Sets", R.drawable.dress_anarkali_1793417013639)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-KCL-28",
            linkedMainCategoryName = "Kids Clothing",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-KCL-01", "Girls Ethnic", R.drawable.dress_lehenga_1793417067159),
                ClothingSubCategorySpec("SUB-KCL-02", "Daily Kids Wear", R.drawable.dress_anarkali_1793417013639)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-BAB-29",
            linkedMainCategoryName = "Baby Clothing",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-BAB-01", "Infant Rompers", R.drawable.dress_anarkali_1793417013639),
                ClothingSubCategorySpec("SUB-BAB-02", "Baby Cotton Frocks", R.drawable.dress_lehenga_1793417067159)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-PLS-30",
            linkedMainCategoryName = "Plus Size Fashion",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-PLS-01", "3XL-5XL Kurtis", R.drawable.dress_anarkali_1793417013639),
                ClothingSubCategorySpec("SUB-PLS-02", "Plus Size Dresses", R.drawable.dress_saree_1793417052582)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-WNT-31",
            linkedMainCategoryName = "Winter Wear",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-WNT-01", "Woolen Kurtis & Shawls", R.drawable.dress_saree_1793417052582),
                ClothingSubCategorySpec("SUB-WNT-02", "Jackets & Sweaters", R.drawable.dress_anarkali_1793417013639)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-INW-32",
            linkedMainCategoryName = "Innerwear & Lingerie",
            subcategories = listOf(
                ClothingSubCategorySpec("SUB-INW-01", "Everyday Comfort", R.drawable.dress_saree_1793417052582)
            )
        ),
        ClothingCategorySpec(
            id = "CAT-FTW-10",
            linkedMainCategoryName = "Footwear",
            subcategories = listOf(
                ClothingSubCategorySpec(
                    "SUB-FTW-01",
                    "Men Shoes",
                    R.drawable.dress_anarkali_1793417013639,
                    keywords = listOf("Men Shoes", "Oxford", "Loafers", "जूते")
                ),
                ClothingSubCategorySpec(
                    "SUB-FTW-02",
                    "Women Shoes",
                    R.drawable.dress_lehenga_1793417067159,
                    keywords = listOf("Women Shoes", "Jutti", "Heels", "Sandals", "चप्पल")
                )
            )
        )
    )

    private val aliasGroups: List<Set<String>> = listOf(
        setOf("saree", "sarees", "banarasi silk", "silk sarees", "ready to wear", "organza", "women's ethnic wear"),
        setOf("kurti", "kurtis", "kurti & suit", "kurtis & suits", "anarkali suits", "kurti palazzo sets", "short kurtis", "chikankari", "suit", "suits"),
        setOf("lehenga", "lehengas", "lehenga choli", "bridal & wedding", "partywear lehenga", "wedding & festive wear", "ethnic wear"),
        setOf("western wear", "gowns & dresses", "gown", "dresses", "girls western wear", "top", "t-shirt", "jeans"),
        setOf("kids", "kids wear", "kids & baby", "kids clothing", "girls", "girls ethnic", "girls ethnic wear", "boys", "boys clothing", "baby clothing"),
        setOf("women", "women's ethnic wear", "womens ethnic wear"),
        setOf("men", "men shoes", "mens wear"),
        setOf("footwear", "shoes", "men shoes", "women shoes", "jutti", "heels")
    )

    fun resolveAliasGroup(target: String): Set<String> {
        val normalized = target.trim().lowercase()
        if (normalized.isBlank()) return emptySet()
        val matched = aliasGroups.filter { group ->
            group.any { token ->
                token == normalized || token.contains(normalized) || normalized.contains(token)
            }
        }.flatten().toMutableSet()
        matched.add(normalized)
        return matched
    }

    fun searchEntries(query: String): List<ClothingCatalogSearchEntry> {
        val q = query.trim().lowercase()
        val allEntries = clothingCategories.flatMap { cat ->
            cat.subcategories.map { sub ->
                ClothingCatalogSearchEntry(
                    linkedMainCategoryName = cat.linkedMainCategoryName,
                    subcategoryName = sub.name,
                    keywords = sub.keywords,
                    imageRes = sub.imageRes
                )
            }
        }
        if (q.isBlank()) return allEntries
        return allEntries.filter { entry ->
            entry.linkedMainCategoryName.lowercase().contains(q) ||
                entry.subcategoryName.lowercase().contains(q) ||
                entry.keywords.any { it.lowercase().contains(q) }
        }
    }
}
