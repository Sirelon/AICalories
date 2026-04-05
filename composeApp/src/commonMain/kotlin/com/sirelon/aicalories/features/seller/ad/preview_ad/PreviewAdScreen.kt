package com.sirelon.aicalories.features.seller.ad.preview_ad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.permissions.CoarseLocation
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.generateRandomColor
import com.sirelon.aicalories.features.media.PermissionDialogContent
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.CategorySelected
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.location.OlxLocation
import kotlinx.coroutines.launch
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PreviewAdScreen(
    advertisement: Advertisement,
    onChangeCategoryClick: () -> Unit,
    pendingCategory: OlxCategory?,
    onCategoryConsumed: () -> Unit,
) {
    val viewModel: PreviewAdViewModel = koinViewModel { parametersOf(advertisement) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pendingCategory) {
        if (pendingCategory != null) {
            viewModel.onEvent(CategorySelected(pendingCategory))
            onCategoryConsumed()
        }
    }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is PreviewAdContract.PreviewAdEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val locationPermissionController = rememberPermissionController(permission = Permission.CoarseLocation)

    PermissionDialogs(
        controller = locationPermissionController,
        rationaleContent = PermissionDialogContent(
            title = Res.string.location_rationale_title,
            message = Res.string.location_rationale_message,
            confirmText = Res.string.retry,
            dismissText = Res.string.not_now,
        ),
        settingsContentProvider = { isIos ->
            PermissionDialogContent(
                title = Res.string.location_settings_title,
                message = if (isIos) {
                    Res.string.location_settings_message_ios
                } else {
                    Res.string.location_settings_message_android
                },
                confirmText = Res.string.open_settings,
                dismissText = Res.string.cancel,
            )
        },
    )

    LaunchedEffect(Unit) {
        locationPermissionController.requestPermission {
            viewModel.onEvent(PreviewAdEvent.FetchLocation)
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
                text = stringResource(Res.string.publish_to_olx),
                trailingIcon = painterResource(Res.drawable.ic_arrow_right),
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
                ) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = state.images[pageIndex],
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                    )
                }
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
                    categoryLabel = state.categoryLabel,
                    onChangeClick = onChangeCategoryClick,
                )

                AdLocationCard(
                    location = state.location,
                    isLoading = state.locationLoading,
                )
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
                            painterResource(Res.drawable.ic_copy),
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.Size.xl3)
                        )
                        Spacer(modifier = Modifier.width(AppDimens.Spacing.l))
                        Text(stringResource(Res.string.copy), style = AppTheme.typography.label)
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
    PreviewSectionCard(icon = null, label = stringResource(Res.string.ad_title_label), textFieldState = titleState) {
    }
}

@Composable
private fun AdDescriptionCard(descriptionState: TextFieldState) {
    PreviewSectionCard(
        icon = null,
        label = stringResource(Res.string.ad_description_label),
        textFieldState = descriptionState,
    ) {
        Box {
            Text(
                text = stringResource(Res.string.character_count, descriptionState.text.length),
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
        icon = null,
        label = stringResource(Res.string.ad_your_price),
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
                        text = stringResource(Res.string.discount_percentage, discount),
                        color = AppTheme.colors.success,
                        style = AppTheme.typography.label.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Text(
                text = stringResource(Res.string.original_price, originalPrice),
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
private fun AdCategoryCard(categoryLabel: String, onChangeClick: () -> Unit) {
    PreviewSectionCard(label = stringResource(Res.string.ad_category_label)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = AppDimens.Spacing.m).clickable(onClick = onChangeClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_layout_grid),
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.Size.xl6),
                    tint = AppTheme.colors.onSurfaceMuted,
                )
                Text(
                    text = categoryLabel,
                    style = AppTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                )
            }
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier
                    .size(AppDimens.Size.xl4)
                    .padding(start = AppDimens.Spacing.m),
                tint = AppTheme.colors.onSurfaceMuted,
            )
        }
    }
}

@Composable
private fun AdLocationCard(
    location: OlxLocation?,
    isLoading: Boolean,
) {
    PreviewSectionCard(label = stringResource(Res.string.ad_location_label)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = AppDimens.Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(AppDimens.Size.xl6),
                tint = AppTheme.colors.onSurfaceMuted,
            )
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.size(AppDimens.Size.xl4))
                    Text(
                        text = stringResource(Res.string.location_detecting),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.outline,
                    )
                }

                location != null -> {
                    Text(
                        text = location.displayName,
                        style = AppTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                    )
                }

                else -> {
                    Text(
                        text = stringResource(Res.string.location_not_available),
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.outline,
                    )
                }
            }
        }
    }
}
