package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.remote.AiInferenceState
import com.example.data.remote.AiSmartSearchResult
import com.example.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    recentSearches: List<String>,
    popularSearches: List<String>,
    matchingProducts: List<Product>,
    wishlistIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onSubmitSearch: (String) -> Unit,
    onClearRecent: () -> Unit,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onVoiceClick: () -> Unit,
    onCameraClick: () -> Unit,
    onBack: () -> Unit,
    allProducts: List<Product> = matchingProducts,
    aiSearchState: AiInferenceState<AiSmartSearchResult> = AiInferenceState.Idle,
    onAiSmartSearch: (String) -> Unit = {},
    onClearAiSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YellowBackground)
            .statusBarsPadding()
            .testTag("search_screen")
    ) {
        // Search Top Header
        Surface(
            color = YellowPrimary,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("search_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                // Search Input Field
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardWhite,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        TextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            placeholder = {
                                Text(
                                    text = "Search by Keyword or Product ID...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF757575),
                                    fontSize = 13.sp
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF111111),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF111111),
                                unfocusedTextColor = Color(0xFF111111),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                cursorColor = Color(0xFF111111),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_text_field")
                        )

                        // Clear 'X' icon if text is not empty
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Microphone Voice Search
                        IconButton(
                            onClick = onVoiceClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("search_voice_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = MeeshoMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Camera Image Search
                        IconButton(
                            onClick = onCameraClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("search_camera_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera Search",
                                tint = MeeshoMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Body Content: If user has entered query, show matching products + AI Smart Search; otherwise show Popular Searches & Banner
        if (searchQuery.isNotBlank()) {
            val aiMatchedProducts = remember(aiSearchState, allProducts) {
                if (aiSearchState is AiInferenceState.Success) {
                    val byId = allProducts.associateBy { it.id }
                    aiSearchState.data.matchedProductIds.mapNotNull { byId[it] }
                } else emptyList()
            }
            val displayProducts = if (aiSearchState is AiInferenceState.Success && aiMatchedProducts.isNotEmpty()) {
                aiMatchedProducts
            } else {
                matchingProducts
            }

            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                // Real AI Product Search Banner (Firebase AI Logic Gemini)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("AI Product Search (Gemini)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagentaDark)
                                    Text("Try Hinglish queries: 'red bridal lehenga under 1500'", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                            Button(
                                onClick = { onAiSmartSearch(searchQuery) },
                                enabled = aiSearchState !is AiInferenceState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("ai_smart_search_btn")
                            ) {
                                Text(
                                    text = if (aiSearchState is AiInferenceState.Loading) "Thinking..." else "Ask AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        when (aiSearchState) {
                            is AiInferenceState.Loading -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MeeshoMagenta)
                                    Text("Analyzing catalog with Gemini AI...", fontSize = 11.sp, color = MeeshoMagentaDark)
                                }
                            }
                            is AiInferenceState.Success -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✨ ${aiSearchState.data.searchSummary}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF166534),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = onClearAiSearch, contentPadding = PaddingValues(horizontal = 6.dp)) {
                                        Text("Reset", fontSize = 10.sp, color = MeeshoMagenta)
                                    }
                                }
                            }
                            is AiInferenceState.Error -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚠️ ${aiSearchState.message}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB91C1C),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { onAiSmartSearch(searchQuery) }) {
                                        Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    }
                                }
                            }
                            AiInferenceState.Idle -> {}
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (aiSearchState is AiInferenceState.Success && aiMatchedProducts.isNotEmpty()) {
                            "AI Matches for \"$searchQuery\" (${displayProducts.size} items)"
                        } else {
                            "Results for \"$searchQuery\" (${displayProducts.size} items)"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    TextButton(onClick = {
                        onClearAiSearch()
                        onQueryChange("")
                    }) {
                        Text("Clear", color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                    }
                }

                if (displayProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No dresses found matching \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try tapping 'Ask AI' above or search Anarkali, Saree, Lehenga, or Kurti",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayProducts, key = { it.id }) { product ->
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
        } else {
            // Default Search Landing Page (Matching user screenshot 8:53)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Recent Searches (if any)
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            TextButton(onClick = onClearRecent) {
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeeshoMagenta
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            recentSearches.forEach { term ->
                                SuggestionChip(
                                    onClick = { onSubmitSearch(term) },
                                    label = { Text(term, style = MaterialTheme.typography.bodySmall) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = CardWhite,
                                        labelColor = TextPrimary
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        borderColor = YellowBorder,
                                        enabled = true
                                    )
                                )
                            }
                        }
                    }
                }

                // Popular Searches Heading
                item {
                    Text(
                        text = "Popular Searches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Popular tags flow matching user screenshot 8:53!
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        popularSearches.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CardWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onSubmitSearch(tag) }
                                    .testTag("popular_search_$tag")
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Meesho Illustrated Shopping Banner (screenshot 8:53)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.search_banner_illustration_1793417120669),
                                contentDescription = "Shop what you love",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF9C4))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Jhumka Bareilly ka ya Saree Banarasi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Shop what you ❤️ on Gyanvi Dresses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MeeshoMagentaDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Surface(
                                    color = MeeshoMagenta,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { onSubmitSearch("saree") }
                                ) {
                                    Text(
                                        text = "Explore",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
