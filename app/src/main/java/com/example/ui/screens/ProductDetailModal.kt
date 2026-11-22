package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.ui.components.AppManagedImage
import com.example.ui.theme.*
import com.example.util.ImageSaver
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProductDetailModal(
    product: Product,
    isWishlisted: Boolean,
    onBack: () -> Unit,
    onWishlistToggle: () -> Unit,
    onAddToCart: (String) -> Unit,
    onBuyNow: (String) -> Unit,
    onShareClick: () -> Unit,
    onCompareClick: () -> Unit = {},
    onOpenAiAssistant: () -> Unit = {},
    similarProducts: List<Product> = emptyList(),
    onProductClick: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSize by remember { mutableStateOf(product.sizes.firstOrNull() ?: "Free Size") }
    var selectedColor by remember { mutableStateOf("Rani Pink") }
    var selectedMediaView by remember { mutableStateOf("Front View") }
    var showSizeChartDialog by remember { mutableStateOf(false) }
    var aiRecommendedSize by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "Product Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onShareClick, modifier = Modifier.testTag("detail_share_btn")) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
                        }
                        IconButton(onClick = onWishlistToggle) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) SaleRed else TextPrimary
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = CardWhite,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAddToCart(selectedSize) },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MeeshoMagenta),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MeeshoMagenta),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Text(
                            text = "Add to Cart",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MeeshoMagenta
                        )
                    }

                    Button(
                        onClick = { onBuyNow(selectedSize) },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_buy_now_btn")
                    ) {
                        Text(
                            text = "Buy Now",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = YellowBackground,
        modifier = modifier.fillMaxSize().testTag("product_detail_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Big Dress Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color(0xFFF0F0F0))
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (product.isMahaDeal) {
                    Surface(
                        color = SaleRed,
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "MAHA DEAL ⚡ UP TO 80% OFF",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Quick Save & Share Action Bar on Image
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save to Gallery button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                ImageSaver.saveToGallery(
                                    context = context,
                                    drawableResId = product.imageRes,
                                    productName = product.name
                                )
                            }
                            .testTag("detail_download_photo_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Download to Gallery",
                                tint = YellowPrimaryDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Save to Gallery",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Share button
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onShareClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MeeshoMagenta,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Info Card
            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "₹${product.price}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )

                        Text(
                            text = "₹${product.originalPrice}",
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = TextDecoration.LineThrough,
                            color = TextMuted
                        )

                        Surface(
                            color = GreenLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${product.discountPercent}% Off",
                                color = GreenRating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "₹${product.codPrice} with Cash on Delivery  •  UPI Discount applied",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ratings pill
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GreenRating,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${product.rating}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${product.ratingCount} Ratings & 4,120 Reviews",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Multi-Angle & 360 View Strip (Module 5D)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Front View", "Back View", "Side View", "360° View").forEach { viewMode ->
                            val active = selectedMediaView == viewMode
                            FilterChip(
                                selected = active,
                                onClick = { selectedMediaView = viewMode },
                                label = { Text(viewMode, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeeshoMagenta,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Compare + Ask AI / Seller Chat Action Pills
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCompareClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("detail_compare_btn"),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compare Dress", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                        }
                        OutlinedButton(
                            onClick = onOpenAiAssistant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("detail_chat_ai_btn"),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI & Seller Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor)

                    // Color Management Swatches (Module 5B)
                    Text(
                        text = "Select Color: $selectedColor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "Rani Pink" to Color(0xFFD81B60),
                            "Wine Maroon" to Color(0xFF880E4F),
                            "Mustard Gold" to Color(0xFFF59E0B),
                            "Royal Blue" to Color(0xFF1D4ED8),
                            "Emerald Green" to Color(0xFF059669)
                        ).forEach { (cName, cColor) ->
                            val isSel = selectedColor == cName
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(cColor)
                                    .border(if (isSel) 3.dp else 1.dp, if (isSel) TextPrimary else Color.LightGray, CircleShape)
                                    .clickable { selectedColor = cName }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor)

                    // Size Selection + Size Chart & AI Size Recommender
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Size",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "✨ AI Size Fit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.clickable {
                                    aiRecommendedSize = product.sizes.getOrElse(1) { product.sizes.firstOrNull() ?: "L" }
                                    aiRecommendedSize?.let { selectedSize = it }
                                }
                            )
                            Text(
                                text = "Size Chart 📏",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MeeshoMagenta,
                                modifier = Modifier.clickable { showSizeChartDialog = true }
                            )
                        }
                    }

                    if (aiRecommendedSize != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✨ AI Size Recommendation: Size $aiRecommendedSize matches 94% of buyers with similar fit!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0D47A1),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        product.sizes.forEach { size ->
                            val isSelected = selectedSize == size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MeeshoMagenta else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isSelected) Color(0xFFFCE4EC) else CardWhite)
                                    .clickable { selectedSize = size }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MeeshoMagenta else TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = DividerColor)

                    // Reseller System Margin Calculator Card (Extra Advanced Feature)
                    var resellerMargin by remember { mutableIntStateOf(150) }
                    val resellPrice = product.price + resellerMargin
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3E5F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💼 Resell & Earn Margin", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4A148C))
                                Surface(color = Color(0xFF4A148C), shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        "Earn +₹$resellerMargin",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                "Wholesale Price: ₹${product.price}  →  Customer Quote: ₹$resellPrice",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(50, 100, 150, 300).forEach { m ->
                                    FilterChip(
                                        selected = resellerMargin == m,
                                        onClick = { resellerMargin = m },
                                        label = { Text("+₹$m", fontSize = 11.sp) }
                                    )
                                }
                            }
                            Button(
                                onClick = onShareClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(38.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Resell Quote (₹$resellPrice)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Product Specifications
                    Text(
                        text = "Product Details & Fabric",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailSpecRow("Fabric", product.fabric)
                    DetailSpecRow("Occasion", "Festive, Wedding, Party & Traditional")
                    DetailSpecRow("Pattern", "Floral Embroidery & Zari Work")
                    DetailSpecRow("Brand", "Gyanvi Dresses Exclusive")
                    DetailSpecRow("Packer Name", product.packerName)
                    DetailSpecRow("Packer ID", product.packerId)
                    DetailSpecRow("Return Policy", "7 Days Return / Exchange on wrong or defective items")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    if (similarProducts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Similar Products (मिलते-जुलते प्रोडक्ट्स)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${similarProducts.size} Matches",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MeeshoMagenta
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("detail_similar_products_row")
                        ) {
                            items(similarProducts, key = { "sim_${it.id}" }) { sim ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .width(142.dp)
                                        .clickable { onProductClick(sim) }
                                        .testTag("similar_product_${sim.id}")
                                ) {
                                    Column {
                                        AppManagedImage(
                                            drawableRes = sim.imageRes,
                                            customUri = sim.customImageUri,
                                            contentDescription = sim.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        )
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = sim.name,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "₹${sim.price}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "${sim.discountPercent}% off",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GreenDiscount
                                                )
                                            }
                                            Text(
                                                text = "${sim.rating} ★ • ${sim.fabric}",
                                                fontSize = 10.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSizeChartDialog) {
        AlertDialog(
            onDismissRequest = { showSizeChartDialog = false },
            title = { Text("Clothing Size Chart (Inches)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Size S: Chest 36\" • Waist 30\" • Length 44\"", fontSize = 12.sp)
                    Text("Size M: Chest 38\" • Waist 32\" • Length 45\"", fontSize = 12.sp)
                    Text("Size L: Chest 40\" • Waist 34\" • Length 46\"", fontSize = 12.sp)
                    Text("Size XL: Chest 42\" • Waist 36\" • Length 46\"", fontSize = 12.sp)
                    Text("Size XXL: Chest 44\" • Waist 38\" • Length 47\"", fontSize = 12.sp)
                    Text("Kids Numeric (16–24): Age 2–12 Years", fontSize = 12.sp, color = MeeshoMagenta)
                }
            },
            confirmButton = {
                Button(onClick = { showSizeChartDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun DetailSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
