package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.OrderItem
import com.example.ui.theme.*

@Composable
fun FeedbackModal(
    order: OrderItem,
    currentStep: Int,
    overallRating: Int,
    materialRating: Int,
    commentText: String,
    onStepChange: (Int) -> Unit,
    onOverallRatingChange: (Int) -> Unit,
    onMaterialRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onStepChange(3)
        }
    }

    val ratingLabels = listOf("Very Bad", "Bad", "Ok-Ok", "Good", "Very Good")

    Scaffold(
        topBar = {
            Surface(color = CardWhite, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("feedback_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "ADD FEEDBACK",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = CardWhite,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    if (currentStep < 3) {
                        OutlinedButton(
                            onClick = { onStepChange(currentStep + 1) },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF616161)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("feedback_skip_btn")
                        ) {
                            Text(
                                text = "Skip / Next",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    } else {
                        Button(
                            onClick = onSubmit,
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("feedback_submit_btn")
                        ) {
                            Text(
                                text = "Submit Feedback",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = CardWhite,
        modifier = modifier.fillMaxSize().testTag("feedback_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 3-Step Stepper (matching user screenshots 8:39)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Step 1: Rating
                StepItem(stepNumber = 1, label = "Rating", isActive = currentStep >= 1, isCurrent = currentStep == 1)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (currentStep > 1) Color(0xFF1976D2) else Color(0xFFE0E0E0))
                )

                // Step 2: Photos & Videos
                StepItem(stepNumber = 2, label = "Photos & Videos", isActive = currentStep >= 2, isCurrent = currentStep == 2)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (currentStep > 2) Color(0xFF1976D2) else Color(0xFFE0E0E0))
                )

                // Step 3: Comment
                StepItem(stepNumber = 3, label = "Comment", isActive = currentStep >= 3, isCurrent = currentStep == 3)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor)

            // Step Content
            when (currentStep) {
                1 -> {
                    // Step 1: Overall Rating & Material Rating (screenshot 8:39)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Overall Stars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            (1..5).forEach { starIndex ->
                                val isSelected = overallRating >= starIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onOverallRatingChange(starIndex) }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isSelected) GreenRating else Color(0xFFBDBDBD),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ratingLabels[starIndex - 1],
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (overallRating == starIndex) TextPrimary else TextSecondary,
                                        fontWeight = if (overallRating == starIndex) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "We are glad you loved the product!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "Tell us more about the Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Material",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Material 5 Stars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            (1..5).forEach { starIndex ->
                                val isSelected = materialRating >= starIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onMaterialRatingChange(starIndex) }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = if (isSelected) GreenRating else Color(0xFF757575),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ratingLabels[starIndex - 1],
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Step 2: Add Photos and Videos (screenshot 8:39 third image)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Add Photos and Videos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Illustration Banner: Show us what your product looks like
                        Surface(
                            color = Color(0xFFE8EAF6),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Show us what your\nproduct looks like",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3949AB)
                                )
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = Color(0xFF3949AB),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Add Photos & Videos Purple Button (screenshot 8:39)
                        Button(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Photos & Videos",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                3 -> {
                    // Step 3: Comment
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Write your review for Gyanvi Dresses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = onCommentChange,
                            placeholder = {
                                Text(
                                    "How was the dress fitting, fabric quality, and stitching? Share your experience...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF757575)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF111111),
                                fontSize = 14.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF111111),
                                unfocusedTextColor = Color(0xFF111111),
                                cursorColor = Color(0xFF111111),
                                focusedBorderColor = MeeshoMagenta,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCurrent: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF1976D2) else Color(0xFFBDBDBD)),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && !isCurrent) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = "$stepNumber",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) Color(0xFF1976D2) else TextSecondary
        )
    }
}
