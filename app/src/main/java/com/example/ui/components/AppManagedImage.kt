package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R

@Composable
fun AppManagedImage(
    @DrawableRes drawableRes: Int?,
    customUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes fallbackRes: Int = R.drawable.gyanvi_hero_banner_1793416998706
) {
    if (!customUri.isNullOrBlank()) {
        AsyncImage(
            model = customUri,
            contentDescription = contentDescription,
            contentScale = contentScale,
            placeholder = painterResource(id = drawableRes ?: fallbackRes),
            error = painterResource(id = drawableRes ?: fallbackRes),
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = drawableRes ?: fallbackRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
