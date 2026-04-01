package com.sirelon.aicalories.features.seller.preview_ad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.generateRandomColor
import kotlinx.coroutines.launch

@Composable
fun PreviewAdScreen() {
    AppScaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val state = rememberCarouselState { 5 }

            HorizontalCenteredHeroCarousel(
                modifier = Modifier.height(300.dp),
                state = state,
            ) { pageIndex ->
                val randomColor = remember(pageIndex) { generateRandomColor() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .maskClip(MaterialTheme.shapes.large)
                        .background(randomColor)
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdTitleCard(title = "Vintage Leather Messenger Bag - Authent")

                AdDescriptionCard(
                    description = "messenger bag. Rich patina develops with age. Spacious main compartment with laptop sleeve, multiple pockets for organization. Perfect for work or casual use. Very durable hardware."
                )

                AdPriceCard(
                    price = 100,
                    originalPrice = 220,
                    minPrice = 43,
                    maxPrice = 128
                )

                AdCategoryCard(category = "Fashion")
            }
        }
    }
}

@Composable
private fun PreviewSectionCard(
    icon: ImageVector? = null,
    label: String,
    textFieldState: TextFieldState? = null,
    content: @Composable () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(AppDimens.Spacing.xl3)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        modifier = Modifier.size(AppDimens.Size.xl4),
                        contentDescription = null,
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                )
                if (textFieldState != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            scope.launch {
                                clipboard.setText(AnnotatedString(textFieldState.text.toString()))
                            }
                        },
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.Size.xl3)
                        )
                        Spacer(modifier = Modifier.width(AppDimens.Spacing.l))
                        Text("Copy", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (textFieldState != null) {
                BasicTextField(
                    state = textFieldState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            content()
        }
    }
}

@Composable
private fun AdTitleCard(title: String) {
    val state = rememberTextFieldState(title)
    PreviewSectionCard(icon = Icons.Default.LocalOffer, label = "Title", textFieldState = state) {

    }
}

@Composable
private fun AdDescriptionCard(description: String) {
    val state = rememberTextFieldState(description)
    PreviewSectionCard(
        icon = Icons.Default.Description,
        label = "Description",
        textFieldState = state,
    ) {
        Box {
            Text(
                text = "${description.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun AdPriceCard(price: Int, originalPrice: Int, minPrice: Int, maxPrice: Int) {
    var currentPrice by remember { mutableStateOf(price.toFloat()) }

    PreviewSectionCard(
        icon = Icons.Default.AttachMoney,
        label = "Your Price",
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TODO: Currency
                Text(
                    "€",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = currentPrice.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val discount = ((1 - (currentPrice / originalPrice)) * 100).toInt()
                    Text(
                        text = "$discount% off",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Text(
                text = "Original: €$originalPrice",
                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = currentPrice,
                onValueChange = { currentPrice = it },
                valueRange = minPrice.toFloat()..maxPrice.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF4511E),
                    activeTrackColor = Color(0xFFF4511E).copy(alpha = 0.24f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "€$minPrice",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    "€$maxPrice",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun AdCategoryCard(category: String) {
    PreviewSectionCard(label = "Category") {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            // TODO:
            TextButton(onClick = {}) {
                Text(
                    text = "Change",
                )
            }

        }
    }
}