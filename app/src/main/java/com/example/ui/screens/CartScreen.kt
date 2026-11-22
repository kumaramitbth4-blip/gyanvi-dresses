package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CartItem
import com.example.model.PaymentGatewayLinkItem
import com.example.model.PaymentGatewayProviderPreset
import com.example.model.PaymentGatewaySettingsConfig
import com.example.model.Product
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    deliveryAddress: String,
    walletBalance: Int = 250,
    rewardPoints: Int = 420,
    onBack: () -> Unit,
    onUpdateQuantity: (String, String, Int) -> Unit,
    onUpdateSize: (String, String, String) -> Unit,
    onRemoveItem: (String, String) -> Unit,
    onMoveToWishlist: (String, String) -> Unit,
    onPlaceOrder: () -> Unit,
    onConfirmPayment: ((paymentMethod: String, discountAmount: Int, walletUsed: Int) -> Unit)? = null,
    onChangeAddress: () -> Unit,
    onOpenWishlist: () -> Unit,
    paymentGatewayConfig: PaymentGatewaySettingsConfig = LocalAppConfig.current.paymentGateways,
    onSaveAndConnectPaymentGateway: ((id: String?, name: String, url: String, provider: PaymentGatewayProviderPreset, lastStatus: String, connectNow: Boolean) -> Result<PaymentGatewayLinkItem>)? = null,
    onConnectPaymentGateway: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var sizeChangeItem by remember { mutableStateOf<CartItem?>(null) }
    var qtyChangeItem by remember { mutableStateOf<CartItem?>(null) }
    var showPaymentModal by remember { mutableStateOf(false) }

    val totalPrice = cartItems.sumOf { it.product.price * it.quantity }
    val totalOriginalPrice = cartItems.sumOf { it.product.originalPrice * it.quantity }
    val totalDiscount = totalOriginalPrice - totalPrice
    val savingsToday = 85

    Scaffold(
        topBar = {
            Surface(
                color = CardWhite,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("cart_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text(
                            text = "CART",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "STEP 1/3",
                            color = Color(0xFF1976D2),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = CardWhite,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "₹$totalPrice",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "VIEW PRICE DETAILS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MeeshoMagenta
                            )
                        }

                        Button(
                            onClick = { showPaymentModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("cart_continue_btn")
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = YellowBackground,
        modifier = modifier.fillMaxSize().testTag("cart_screen")
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = YellowPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Your Cart is Empty",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore trendy ethnic kurtis, sarees and gowns",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                    ) {
                        Text("Start Shopping", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Delivery Address Strip (screenshot 8:40)
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MeeshoMagenta,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Delivery at $deliveryAddress",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onChangeAddress,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Change",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // 2. "₹85 Less today" Green Savings Alert Banner (screenshot 8:40)
                item {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = GreenRating,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹$savingsToday Less today with Gyanvi Dhamaka Offer",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenRating
                            )
                        }
                    }
                }

                // 3. Cart Items
                items(cartItems, key = { "${it.product.id}_${it.selectedSize}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Product Thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = item.product.imageRes),
                                        contentDescription = item.product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    // Sale tag
                                    Surface(
                                        color = SaleRed,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "SALE ⚡",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Price Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "₹${item.product.price}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "₹${item.product.originalPrice}",
                                            style = MaterialTheme.typography.bodySmall,
                                            textDecoration = TextDecoration.LineThrough,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "${item.product.discountPercent}% Off",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GreenRating,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Size & Qty Pills (screenshot 8:40)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // Size button
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF5F5F5),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                            modifier = Modifier.clickable { sizeChangeItem = item }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Size: ${item.selectedSize}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // Qty button
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF5F5F5),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                            modifier = Modifier.clickable { qtyChangeItem = item }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Qty: ${item.quantity}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Only wrong/defect item returns accepted",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            Text(
                                text = "🚚 Estimated Delivery in 3-4 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)

                            // Action buttons: Move to Wishlist | Remove (screenshot 8:40)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { onMoveToWishlist(item.product.id, item.selectedSize) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Move to Wishlist",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }

                                VerticalDivider(modifier = Modifier.height(20.dp), color = DividerColor)

                                Row(
                                    modifier = Modifier
                                        .clickable { onRemoveItem(item.product.id, item.selectedSize) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Remove",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Quick Wishlist Shortcut Link
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenWishlist() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = MeeshoMagenta)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "View Wishlist Products",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                        }
                    }
                }

                // 5. Price Details Breakdown (screenshot 8:40)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Price Details (${cartItems.size} Item${if (cartItems.size > 1) "s" else ""})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Product Price",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "+ ₹$totalOriginalPrice",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Discounts",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "- ₹$totalDiscount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenRating,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Delivery Charges",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "FREE",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenRating,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Order Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "₹$totalPrice",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Select Size Dialog
    sizeChangeItem?.let { item ->
        AlertDialog(
            onDismissRequest = { sizeChangeItem = null },
            title = { Text("Select Size", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    item.product.sizes.forEach { size ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateSize(item.product.id, item.selectedSize, size)
                                    sizeChangeItem = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = item.selectedSize == size,
                                onClick = {
                                    onUpdateSize(item.product.id, item.selectedSize, size)
                                    sizeChangeItem = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = size, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sizeChangeItem = null }) {
                    Text("Close", color = MeeshoMagenta)
                }
            }
        )
    }

    // Modal: Select Quantity Dialog
    qtyChangeItem?.let { item ->
        AlertDialog(
            onDismissRequest = { qtyChangeItem = null },
            title = { Text("Select Quantity", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    (1..5).forEach { qty ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateQuantity(item.product.id, item.selectedSize, qty)
                                    qtyChangeItem = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = item.quantity == qty,
                                onClick = {
                                    onUpdateQuantity(item.product.id, item.selectedSize, qty)
                                    qtyChangeItem = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "$qty", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { qtyChangeItem = null }) {
                    Text("Close", color = MeeshoMagenta)
                }
            }
        )
    }

    if (showPaymentModal) {
        CheckoutPaymentModal(
            subtotal = totalPrice,
            deliveryAddress = deliveryAddress,
            walletBalance = walletBalance,
            rewardPoints = rewardPoints,
            onDismiss = { showPaymentModal = false },
            onConfirmPayment = { method, discount, walletUsed ->
                showPaymentModal = false
                if (onConfirmPayment != null) {
                    onConfirmPayment(method, discount, walletUsed)
                } else {
                    onPlaceOrder()
                }
            },
            paymentGatewayConfig = paymentGatewayConfig,
            onSaveAndConnectPaymentGateway = onSaveAndConnectPaymentGateway,
            onConnectPaymentGateway = onConnectPaymentGateway
        )
    }
}
