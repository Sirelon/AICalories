package com.sirelon.aicalories.features.seller.ad.preview_ad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.generateRandomColor
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PreviewAdScreen(advertisement: Advertisement) {
    val viewModel: PreviewAdViewModel = koinViewModel { parametersOf(advertisement) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is PreviewAdContract.PreviewAdEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = AppDimens.Spacing.xl3),
                style = AppButtonDefaults.secondary(),
                text = "Publish to OLX",
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = { viewModel.onEvent(PreviewAdEvent.Publish) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        ) {
            val carouselState = rememberCarouselState { state.images.size }

            HorizontalCenteredHeroCarousel(
                modifier = Modifier.height(AppDimens.Size.xl24),
                state = carouselState,
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
                AdTitleCard(titleState = viewModel.titleState)

                AdDescriptionCard(descriptionState = viewModel.descriptionState)

                AdPriceCard(
                    price = viewModel.selectedPrice,
                    onPriceChange = { viewModel.selectedPrice = it },
                    originalPrice = state.originalPrice,
                    minPrice = state.minPrice,
                    maxPrice = state.maxPrice,
                )

                AdCategoryCard(
                    category = state.category,
                    onChangeClick = { viewModel.onEvent(PreviewAdEvent.ShowCategoryPicker) },
                )
            }
        }
    }

    if (state.isCategoryPickerVisible) {
        CategoryPickerDialog(
            categories = state.availableCategories,
            selectedCategory = state.category,
            onCategorySelected = { viewModel.onEvent(PreviewAdEvent.CategoryChanged(it)) },
            onDismiss = { viewModel.onEvent(PreviewAdEvent.DismissCategoryPicker) },
        )
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
private fun AdTitleCard(titleState: TextFieldState) {
    PreviewSectionCard(icon = Icons.Default.LocalOffer, label = "Title", textFieldState = titleState) {
    }
}

@Composable
private fun AdDescriptionCard(descriptionState: TextFieldState) {
    PreviewSectionCard(
        icon = Icons.Default.Description,
        label = "Description",
        textFieldState = descriptionState,
    ) {
        Box {
            Text(
                text = "${descriptionState.text.length} characters",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.outline,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun AdPriceCard(
    price: Float,
    onPriceChange: (Float) -> Unit,
    originalPrice: Double,
    minPrice: Float,
    maxPrice: Float,
) {
    PreviewSectionCard(
        icon = Icons.Default.AttachMoney,
        label = "Your Price",
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "€",
                    style = AppTheme.typography.title,
                )
                Text(
                    text = price.toInt().toString(),
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
                    val discount = ((1 - (price / originalPrice)) * 100).toInt()
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
                value = price,
                onValueChange = onPriceChange,
                valueRange = minPrice..maxPrice,
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
                    "€${minPrice.toInt()}",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.outline
                )
                Text(
                    "€${maxPrice.toInt()}",
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.outline
                )
            }
        }
    }
}

@Composable
private fun AdCategoryCard(category: String, onChangeClick: () -> Unit) {
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
            TextButton(onClick = onChangeClick) {
                Text(text = "Change")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPickerDialog(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Category") },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
