package com.sirelon.aicalories.features.seller.ad.preview_ad

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.calf.permissions.CoarseLocation
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppAsyncImage
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.InputWithCopy
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.features.media.PermissionDialogContent
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.seller.ad.AdvertisementWithAttributes
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
import com.sirelon.aicalories.generated.resources.ic_camera
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

private val PhotoCarouselShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = AppDimens.BorderRadius.xl11,
    bottomEnd = AppDimens.BorderRadius.xl11,
)

@Composable
fun PreviewAdScreen(
    advertisement: AdvertisementWithAttributes,
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

            is PreviewAdContract.PreviewAdEffect.PublishSuccess -> {
                val message = if (effect.advertUrl != null) {
                    "Ad published! View at: ${effect.advertUrl}"
                } else {
                    "Ad published successfully."
                }
                snackbarHostState.showSnackbar(message)
            }
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
                trailingIcon = if (state.isPublishing) null else painterResource(Res.drawable.ic_arrow_right),
                enabled = !state.isPublishing,
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
            PhotoCarousel(images = state.images)

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
fun PhotoCarousel(
    images: List<String>,
    modifier: Modifier = Modifier,
) {
    val containerModifier = modifier
        .fillMaxWidth()
        .height(AppDimens.Size.xl25)
        .clip(PhotoCarouselShape)
        .background(AppTheme.colors.surfaceLow)

    if (images.isEmpty()) {
        EmptyPhotoCarousel(modifier = containerModifier)
        return
    }

    Box(
        modifier = containerModifier,
    ) {
        val pagerState = rememberPagerState(pageCount = { images.size })

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
        ) { pageIndex ->
            PhotoCarouselPage(image = images[pageIndex])
        }

        if (images.size > 1) {
            PageDots(
                pageCount = images.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = AppDimens.Spacing.xl3),
            )
        }
    }
}

@Composable
private fun PhotoCarouselPage(image: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // TODO(SIR-41): Add tap-to-open fullscreen lightbox.
        AppAsyncImage(
            model = image,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun EmptyPhotoCarousel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_camera),
            contentDescription = null,
            tint = AppTheme.colors.onSurfaceMuted,
            modifier = Modifier.size(AppDimens.Size.xl12),
        )
    }
}

@Composable
private fun PageDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { pageIndex ->
            val dotWidth by animateDpAsState(
                targetValue = if (pageIndex == currentPage) AppDimens.Size.xl4 else AppDimens.Size.s,
                label = "pageDotWidth",
            )

            Box(
                modifier = Modifier
                    .size(width = dotWidth, height = AppDimens.Size.s)
                    .clip(CircleShape)
                    .background(
                        color = if (pageIndex == currentPage) {
                            Color.White
                        } else {
                            Color.White.copy(alpha = 0.55f)
                        }
                    )
            )
        }
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
                    thumbColor = AppTheme.colors.primary,
                    activeTrackColor = AppTheme.colors.primary.copy(alpha = 0.24f)
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

@PreviewLightDark
@Composable
private fun PhotoCarouselEmptyPreview() {
    PhotoCarouselPreviewSurface {
        PhotoCarousel(images = emptyList())
    }
}

@PreviewLightDark
@Composable
private fun PhotoCarouselSinglePreview() {
    PhotoCarouselPreviewSurface {
        PhotoCarousel(images = photoCarouselPreviewImages.take(1))
    }
}

@PreviewLightDark
@Composable
private fun PhotoCarouselThreeImagesPreview() {
    PhotoCarouselPreviewSurface {
        PhotoCarousel(images = photoCarouselPreviewImages.take(3))
    }
}

@PreviewLightDark
@Composable
private fun PhotoCarouselEightImagesPreview() {
    PhotoCarouselPreviewSurface {
        PhotoCarousel(images = List(8) { index -> photoCarouselPreviewImages[index % photoCarouselPreviewImages.size] })
    }
}

@Composable
private fun PhotoCarouselPreviewSurface(
    content: @Composable () -> Unit,
) {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.background,
        ) {
            Box(modifier = Modifier.padding(bottom = AppDimens.Spacing.xl5)) {
                content()
            }
        }
    }
}

private val photoCarouselPreviewImages = listOf(
    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=1200",
    "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=1200",
    "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=1200",
)
