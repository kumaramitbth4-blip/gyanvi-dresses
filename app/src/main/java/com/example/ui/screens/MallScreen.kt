package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

data class MallCategoryCard(
    val id: String,
    val title: String,
    val imageRes: Int,
    val brands: String
)

@Composable
fun MallScreen(
    products: List<Product>,
    wishlistIds: Set<String>,
    deliveryLocation: String,
    wishlistCount: Int,
    cartCount: Int,
    onSearchClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onCartClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val mallCategories = listOf(
        MallCategoryCard("mc_1", "Heritage Sarees", R.drawable.dress_saree_1793417052582, "Gyanvi • Kanjivaram"),
        MallCategoryCard("mc_2", "Designer Anarkali", R.drawable.dress_anarkali_1793417013639, "Libas • Biba"),
        MallCategoryCard("mc_3", "Bridal Lehengas", R.drawable.dress_lehenga_1793417067159, "Sabyasachi Art • Manyavar"),
        MallCategoryCard("mc_4", "Festive Kurti Sets", R.drawable.dress_anarkali_1793417013639, "Aurelia • W"),
        MallCategoryCard("mc_5", "Banarasi Weaves", R.drawable.dress_saree_1793417052582, "Varanasi Silk • Gyanvi"),
        MallCategoryCard("mc_6", "Wedding Gowns", R.drawable.dress_lehenga_1793417067159, "Indo-Western • Royal")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YellowBackground)
            .statusBarsPadding()
            .testTag("mall_screen")
    ) {
        // Mall Top Header (Meesho style)
        Surface(
            color = CardWhite,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GYANVI MALL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MeeshoPurple,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "✓ 100% Genuine",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                        }
                        IconButton(onClick = onWishlistClick) {
                            BadgedBox(
                                badge = {
                                    if (wishlistCount > 0) {
                                        Badge(containerColor = MeeshoMagenta, contentColor = Color.White) {
                                            Text("$wishlistCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Wishlist", tint = TextPrimary)
                            }
                        }
                        IconButton(onClick = onCartClick) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(containerColor = MeeshoMagenta, contentColor = Color.White) {
                                            Text("$cartCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart", tint = TextPrimary)
                            }
                        }
                    }
                }

                // Delivery Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "📍 Delivering to $deliveryLocation >",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Mall Categories & Products Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Mall Header Banner
            item(span = { GridItemSpan(2) }) {
                Surface(
                    color = Color(0xFFF3E5F5),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Gyanvi Verified Mall Exclusives",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MeeshoMagentaDark
                            )
                            Text(
                                text = "Direct from Master Artisans & Weavers",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MeeshoMagenta,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Top Categories title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Top Mall Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // 6 Mall Category Cards (screenshot 8:35 third image)
            items(mallCategories) { mc ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = mc.imageRes),
                            contentDescription = mc.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top Badges: SALE UP TO 80% OFF & Mall
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = SaleRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SALE 80% OFF",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = MeeshoPurple,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "✓ Mall",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Bottom Gradient Brand Pill
                        Surface(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = mc.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = mc.brands,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeeshoMagenta,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // Products section
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Mall Trending Dresses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            items(products.filter { it.isMallAssured }, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isWishlisted = wishlistIds.contains(product.id),
                    onProductClick = { onProductClick(product) },
                    onWishlistToggle = { onWishlistToggle(product.id) },
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}
