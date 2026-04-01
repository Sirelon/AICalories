package com.sirelon.aicalories.features.seller.ad.preview_ad

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.generateRandomColor
import com.sirelon.aicalories.features.seller.ad.Advertisement
import kotlinx.coroutines.launch

@Composable
fun PreviewAdScreen(advertisement: Advertisement) {
    AppScaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        ) {
            val state = rememberCarouselState { advertisement.images.size }

            HorizontalCenteredHeroCarousel(
                modifier = Modifier.height(AppDimens.Size.xl24),
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
                modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl)
            ) {
                AdTitleCard(title = advertisement.title)

                AdDescriptionCard(description = advertisement.description)

                AdPriceCard(
                    price = advertisement.suggestedPrice,
                    originalPrice = advertisement.suggestedPrice, // TODO:
                    minPrice = advertisement.minPrice,
                    maxPrice = advertisement.maxPrice,
                )

                AdCategoryCard(category = advertisement.category)
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
                    style = AppTheme.typography.label,
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
                        Text("Copy", style = AppTheme.typography.label)
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
                style = AppTheme.typography.caption,
                color = AppTheme.colors.outline,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun AdPriceCard(price: Double, originalPrice: Double, minPrice: Double, maxPrice: Double) {
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
                    style = AppTheme.typography.title,
                )
                Text(
                    text = currentPrice.toInt().toString(),
                    style = AppTheme.typography.headline.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(
                            AppTheme.colors.success.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = AppDimens.Spacing.m, vertical = AppDimens.Spacing.xs)
                ) {
                    val discount = ((1 - (currentPrice / originalPrice)) * 100).toInt()
                    Text(
                        text = "$discount% off",
                        color = AppTheme.colors.success,
                        style = AppTheme.typography.label.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Text(
                text = "Original: €$originalPrice",
                style = AppTheme.typography.body.copy(textDecoration = TextDecoration.LineThrough),
                color = AppTheme.colors.outline
            )

            Spacer(modifier = Modifier.height(AppDimens.Spacing.xl3))

            Slider(
                value = currentPrice,
                onValueChange = { currentPrice = it },
                valueRange = minPrice.toFloat()..maxPrice.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = AppTheme.colors.warning,
                    activeTrackColor = AppTheme.colors.warning.copy(alpha = 0.24f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "€$minPrice",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.outline
                )
                Text(
                    "€$maxPrice",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.outline
                )
            }
        }
    }
}

@Composable
private fun AdCategoryCard(category: String) {
    PreviewSectionCard(label = "Category") {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = AppDimens.Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = AppTheme.typography.title.copy(fontWeight = FontWeight.Bold)
            )
            // TODO:
            TextButton(onClick = {
                // TODO:
            }) {
                Text(
                    text = "Change",
                )
            }

        }
    }
}
