package com.sirelon.aicalories.features.seller.ad.preview_ad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.permissions.CoarseLocation
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.InputWithCopy
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.utils.generateRandomColor
import com.sirelon.aicalories.features.media.PermissionDialogContent
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.CategorySelected
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.categories.ui.AttributeItem
import com.sirelon.aicalories.features.seller.location.OlxLocation
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.ad_attributes_label
import com.sirelon.aicalories.generated.resources.ad_category_label
import com.sirelon.aicalories.generated.resources.ad_description_label
import com.sirelon.aicalories.generated.resources.ad_location_label
import com.sirelon.aicalories.generated.resources.ad_title_label
import com.sirelon.aicalories.generated.resources.ad_your_price
import com.sirelon.aicalories.generated.resources.cancel
import com.sirelon.aicalories.generated.resources.ic_arrow_right
import com.sirelon.aicalories.generated.resources.ic_chevron_right
import com.sirelon.aicalories.generated.resources.ic_layout_grid
import com.sirelon.aicalories.generated.resources.location_detecting
import com.sirelon.aicalories.generated.resources.location_not_available
import com.sirelon.aicalories.generated.resources.location_rationale_message
import com.sirelon.aicalories.generated.resources.location_rationale_title
import com.sirelon.aicalories.generated.resources.location_settings_message_android
import com.sirelon.aicalories.generated.resources.location_settings_message_ios
import com.sirelon.aicalories.generated.resources.location_settings_title
import com.sirelon.aicalories.generated.resources.not_now
import com.sirelon.aicalories.generated.resources.open_settings
import com.sirelon.aicalories.generated.resources.publish_to_olx
import com.sirelon.aicalories.generated.resources.retry
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

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

            PreviewAdContract.PreviewAdEffect.GoToGategoryPicker -> onChangeCategoryClick()
        }
    }

    LocationPermissionsBlock(viewModel::onEvent)

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
            ImagesCarousel(images = state.images)

            PreviewAdContent(
                onEvent = viewModel::onEvent,
                state = state,
                titleState = viewModel.titleState,
                descriptionState = viewModel.descriptionState
            )
        }
    }
}

@Composable
private fun LocationPermissionsBlock(onEvent: (PreviewAdEvent) -> Unit) {
    val locationPermissionController =
        rememberPermissionController(permission = Permission.CoarseLocation)

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
            onEvent(PreviewAdEvent.FetchLocation)
        }
    }
}

@Composable
private fun PreviewAdContent(
    titleState: TextFieldState,
    descriptionState: TextFieldState,
    state: PreviewAdContract.PreviewAdState,
    onEvent: (PreviewAdEvent) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl)
    ) {
        AdTitleCard(titleState = titleState)

        AdDescriptionCard(descriptionState = descriptionState)

        val priceTextFieldState = rememberTextFieldState(state.price.roundToInt().toString())

        LaunchedEffect(null) {
            snapshotFlow {
                priceTextFieldState.text
            }
                .distinctUntilChanged()
                .map { it.toString().toFloatOrNull() }
                .filterNotNull()
                .collect {
                    onEvent(PreviewAdEvent.OnPriceChanged(it))
                }
        }

        AdPriceCard(
            priceTextFieldState = priceTextFieldState,
            minPrice = state.minPrice,
            maxPrice = state.maxPrice,
        )

        AdCategoryCard(
            categoryLabel = state.categoryLabel,
            onChangeClick = { onEvent(PreviewAdEvent.OnChangeCategoryClick) },
        )

        if (state.attributeItems.isNotEmpty()) {
            AdAttributesCard(
                items = state.attributeItems,
                onEvent = onEvent,
            )
        }

        AdLocationCard(
            location = state.location,
            isLoading = state.locationLoading,
        )
    }
}

@Composable
private fun ImagesCarousel(images: List<String>) {
    val carouselState = rememberCarouselState { images.size }

    HorizontalCenteredHeroCarousel(
        modifier = Modifier.height(AppDimens.Size.xl24),
        state = carouselState,
    ) { pageIndex ->
        val randomColor = remember(pageIndex) { generateRandomColor() }
        CarouselItem(background = randomColor, image = images[pageIndex])
    }
}

@Composable
private fun CarouselItemScope.CarouselItem(
    background: Color,
    image: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .maskClip(MaterialTheme.shapes.large)
            .background(background)
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = image,
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
        )
    }
}

@Composable
private fun PreviewSectionCard(
    label: String,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        onClick = {
            onClick?.invoke()
        },
    ) {
        Column(modifier = Modifier.padding(vertical = AppDimens.Spacing.xl3)) {
            Text(
                modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                text = label,
                style = AppTheme.typography.subTitle,
            )

            content()
        }
    }
}

@Composable
private fun PreviewSectionInputCard(
    label: String,
    textFieldState: TextFieldState,
    maxCharacters: Int,
) {
    PreviewSectionCard(
        label = label,
        content = {
            InputWithCopy(
                state = textFieldState,
                maxCharacters = maxCharacters,
            )
        }
    )
}

@Composable
private fun AdTitleCard(titleState: TextFieldState) {
    PreviewSectionInputCard(
        label = stringResource(Res.string.ad_title_label),
        textFieldState = titleState,
        maxCharacters = 140,
    )
}

@Composable
private fun AdDescriptionCard(descriptionState: TextFieldState) {
    PreviewSectionInputCard(
        label = stringResource(Res.string.ad_description_label),
        textFieldState = descriptionState,
        maxCharacters = 9000,
    )
}

@Composable
private fun AdPriceCard(
    priceTextFieldState: TextFieldState,
    minPrice: Float,
    maxPrice: Float,
) {
    PreviewSectionCard(
        label = stringResource(Res.string.ad_your_price),
    ) {
        Column {
            val textStyle = AppTheme.typography.headline
            ProvideTextStyle(textStyle) {
                InputWithCopy(
                    state = priceTextFieldState,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    prefix = {
                        // TODO: change currency
                        Text(text = "$", style = textStyle)
                    },
                )
            }

            val price = remember(priceTextFieldState.text) {
                (priceTextFieldState.text.toString().toFloatOrNull() ?: ((maxPrice + minPrice) / 2))
            }
            Slider(
                modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                value = price,
                onValueChange = {
                    priceTextFieldState.setTextAndPlaceCursorAtEnd(it.fastRoundToInt().toString())
                },
                valueRange = minPrice..maxPrice,
                colors = SliderDefaults.colors(
                    thumbColor = AppTheme.colors.warning,
                    activeTrackColor = AppTheme.colors.warning.copy(alpha = 0.24f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppDimens.Spacing.xl3),
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
private fun PreviewSectionClickableCard(
    label: String,
    icon: Painter,
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    PreviewSectionCard(label = label, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = AppDimens.Spacing.xl3)
                .padding(top = AppDimens.Spacing.xl3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.Size.xl6),
                    tint = AppTheme.colors.onSurfaceMuted,
                )
                content()
            }
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier,
                tint = AppTheme.colors.onSurfaceMuted,
            )
        }
    }
}


@Composable
private fun AdCategoryCard(categoryLabel: String, onChangeClick: () -> Unit) {
    PreviewSectionClickableCard(
        label = stringResource(Res.string.ad_category_label),
        onClick = onChangeClick,
        icon = painterResource(Res.drawable.ic_layout_grid),
        content = {
            Text(
                text = categoryLabel,
                style = AppTheme.typography.body,
                fontWeight = FontWeight.Bold,
            )
        }
    )
}

@Composable
private fun AdAttributesCard(
    items: List<OlxAttributeState>,
    onEvent: (PreviewAdEvent) -> Unit,
) {
    PreviewSectionCard(label = stringResource(Res.string.ad_attributes_label)) {
        Column {
            items.forEach { item ->
                AttributeItem(
                    attribute = item.attribute,
                    selectedValues = item.selectedValues,
                    onSelectionChange = { values ->
                        onEvent(PreviewAdEvent.AttributeValueChanged(item.attribute.code, values))
                    },
                    validationError = item.error,
                )
            }
        }
    }
}

@Composable
private fun AdLocationCard(
    location: OlxLocation?,
    isLoading: Boolean,
) {
    PreviewSectionClickableCard(
        label = stringResource(Res.string.ad_location_label),
        // TODO: not implemented yet
        onClick = { },
        icon = rememberVectorPainter(Icons.Default.LocationOn),
        content = {
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
                        style = AppTheme.typography.body,
//                        fontWeight = FontWeight.Bold,
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
        },
    )
}
