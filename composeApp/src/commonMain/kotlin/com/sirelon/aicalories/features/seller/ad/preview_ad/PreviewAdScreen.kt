package com.sirelon.aicalories.features.seller.ad.preview_ad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.sirelon.aicalories.designsystem.AppCard
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.AiGeneratedBadge
import com.sirelon.aicalories.designsystem.CopyPill
import com.sirelon.aicalories.designsystem.DigitOnlyInputTransformation
import com.sirelon.aicalories.designsystem.ErrorPill
import com.sirelon.aicalories.designsystem.InputWithCopy
import com.sirelon.aicalories.designsystem.ObserveAsEvents
import com.sirelon.aicalories.designsystem.ThousandSeparatorOutputTransformation
import com.sirelon.aicalories.designsystem.formatPrice
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.features.media.PermissionDialogContent
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.seller.ad.AdvertisementWithAttributes
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.CategorySelected
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.categories.domain.ValidationError
import com.sirelon.aicalories.features.seller.categories.ui.AttributeItem
import com.sirelon.aicalories.features.seller.location.OlxLocation
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.ad_attributes_label
import com.sirelon.aicalories.generated.resources.ad_category_label
import com.sirelon.aicalories.generated.resources.ad_description_label
import com.sirelon.aicalories.generated.resources.ad_location_label
import com.sirelon.aicalories.generated.resources.ad_title_label
import com.sirelon.aicalories.generated.resources.ad_price_ai_estimated_range
import com.sirelon.aicalories.generated.resources.ad_your_price
import com.sirelon.aicalories.generated.resources.cancel
import com.sirelon.aicalories.generated.resources.ic_arrow_right
import com.sirelon.aicalories.generated.resources.ic_camera
import com.sirelon.aicalories.generated.resources.ic_chevron_right
import com.sirelon.aicalories.generated.resources.ic_circle_alert
import com.sirelon.aicalories.generated.resources.ic_circle_check_big
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
import com.sirelon.aicalories.generated.resources.error_attr_above_maximum
import com.sirelon.aicalories.generated.resources.error_attr_below_minimum
import com.sirelon.aicalories.generated.resources.error_attr_invalid_selection
import com.sirelon.aicalories.generated.resources.error_attr_multiple_values_not_allowed
import com.sirelon.aicalories.generated.resources.error_attr_must_be_numeric
import com.sirelon.aicalories.generated.resources.error_attr_required
import com.sirelon.aicalories.generated.resources.publish_errors
import com.sirelon.aicalories.generated.resources.publish_on_olx
import com.sirelon.aicalories.generated.resources.retry
import com.sirelon.aicalories.generated.resources.validation_all_valid
import com.sirelon.aicalories.generated.resources.validation_error_desc_too_short
import com.sirelon.aicalories.generated.resources.validation_error_no_category
import com.sirelon.aicalories.generated.resources.validation_error_no_location
import com.sirelon.aicalories.generated.resources.validation_error_title_too_short
import com.sirelon.aicalories.generated.resources.validation_errors_more
import com.sirelon.aicalories.generated.resources.validation_fields_remaining
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

private const val TitleMinLength = 10
private const val DescriptionMinLength = 30

@Composable
fun PreviewAdScreen(
    advertisement: AdvertisementWithAttributes,
    onChangeCategoryClick: () -> Unit,
    onPublishSuccess: (
        url: String,
        title: String,
        priceFormatted: String,
        primaryImageUrl: String?,
    ) -> Unit,
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
                onPublishSuccess(
                    effect.advertUrl.orEmpty(),
                    viewModel.titleState.text.toString(),
                    "₴ ${formatPrice(state.price)}",
                    state.images.firstOrNull(),
                )
            }
        }
    }

    LocationPermissionsBlock(viewModel::onEvent)

    val titleTooShortLabel = stringResource(Res.string.validation_error_title_too_short)
    val descTooShortLabel = stringResource(Res.string.validation_error_desc_too_short)
    val noCategoryLabel = stringResource(Res.string.validation_error_no_category)
    val noLocationLabel = stringResource(Res.string.validation_error_no_location)

    // @Composable reads of TextFieldState.text trigger recomposition on change.
    val titleText = viewModel.titleState.text
    val descText = viewModel.descriptionState.text
    val validationErrors = buildList {
        if (titleText.length < 10) add(titleTooShortLabel)
        if (descText.length < 30) add(descTooShortLabel)
        if (state.selectedCategory == null) add(noCategoryLabel)
        if (state.location == null) add(noLocationLabel)
        for (item in state.attributeItems) {
            when {
                item.error != null ->
                    add("${item.attribute.label}: ${item.error.toDisplayString()}")
                item.attribute.validationRules.required && item.selectedValues.isEmpty() ->
                    add(item.attribute.label)
            }
        }
    }
    val isValid = validationErrors.isEmpty()

    var showErrors by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = AppDimens.Spacing.xl3)
                    .padding(bottom = AppDimens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
            ) {
                ValidationStatusCard(
                    isValid = isValid,
                    errorCount = validationErrors.size,
                )
                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    style = if (isValid) AppButtonDefaults.success() else AppButtonDefaults.primary(),
                    text = if (isValid) {
                        stringResource(Res.string.publish_on_olx)
                    } else {
                        stringResource(Res.string.publish_errors, validationErrors.size)
                    },
                    trailingIcon = if (state.isPublishing) null else painterResource(Res.drawable.ic_arrow_right),
                    enabled = !state.isPublishing,
                    onClick = {
                        if (!isValid) {
                            showErrors = true
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                            // TODO(SIR-34): auto-open the first failing required attribute editor
                        } else {
                            viewModel.onEvent(PreviewAdEvent.Publish)
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        ) {
            AnimatedVisibility(
                visible = showErrors && !isValid,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                ValidationBanner(
                    errors = validationErrors,
                    modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                )
            }

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
private fun ValidationBanner(
    errors: List<String>,
    modifier: Modifier = Modifier,
) {
    val errorColor = AppTheme.colors.error
    val displayErrors = errors.take(3)
    val remaining = errors.size - displayErrors.size

    AppCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = errorColor.copy(alpha = 0.12f),
        contentColor = errorColor,
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_circle_alert),
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier
                    .size(AppDimens.Size.xl5)
                    .padding(top = AppDimens.Spacing.xs2),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xs)) {
                displayErrors.forEach { error ->
                    Text(
                        text = "• $error",
                        style = AppTheme.typography.body,
                        color = errorColor,
                    )
                }
                if (remaining > 0) {
                    Text(
                        text = stringResource(Res.string.validation_errors_more, remaining),
                        style = AppTheme.typography.caption,
                        color = errorColor.copy(alpha = 0.70f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ValidationStatusCard(
    isValid: Boolean,
    errorCount: Int,
    modifier: Modifier = Modifier,
) {
    val bgColor: Color
    val contentColor: Color
    val icon: Painter
    val text: String

    if (isValid) {
        bgColor = AppTheme.colors.success.copy(alpha = 0.12f)
        contentColor = AppTheme.colors.success
        icon = painterResource(Res.drawable.ic_circle_check_big)
        text = stringResource(Res.string.validation_all_valid)
    } else {
        bgColor = AppTheme.colors.warning.copy(alpha = 0.12f)
        contentColor = AppTheme.colors.warning
        icon = painterResource(Res.drawable.ic_circle_alert)
        text = stringResource(Res.string.validation_fields_remaining, errorCount)
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = bgColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = AppDimens.Spacing.xl3)
                .padding(vertical = AppDimens.Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(AppDimens.Size.xl5),
            )
            Text(
                text = text,
                style = AppTheme.typography.body,
                color = contentColor,
                fontWeight = FontWeight.Medium,
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
    val titleText = titleState.text.toString()
    val descriptionText = descriptionState.text.toString()
    val isTitleInvalid = remember(titleText) { titleText.trim().length < TitleMinLength }
    val isDescriptionInvalid = remember(descriptionText) { descriptionText.trim().length < DescriptionMinLength }

    Column(
        modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl)
    ) {
        AdTitleCard(
            titleState = titleState,
            isInvalid = isTitleInvalid,
        )

        AdDescriptionCard(
            descriptionState = descriptionState,
            isInvalid = isDescriptionInvalid,
        )

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
    headerTrailing: (@Composable () -> Unit)? = null,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.Spacing.xl3),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                    style = AppTheme.typography.subTitle,
                )
                if (headerTrailing != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        headerTrailing()
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun PreviewSectionInputCard(
    label: String,
    textFieldState: TextFieldState,
    maxCharacters: Int,
    isInvalid: Boolean = false,
    headerTrailing: (@Composable () -> Unit)? = null,
) {
    PreviewSectionCard(
        label = label,
        headerTrailing = headerTrailing,
        content = {
            InputWithCopy(
                state = textFieldState,
                maxCharacters = maxCharacters,
                isError = isInvalid,
                showTrailingCopyButton = false,
            )
        }
    )
}

@Composable
private fun AdTitleCard(
    titleState: TextFieldState,
    isInvalid: Boolean,
) {
    PreviewSectionInputCard(
        label = stringResource(Res.string.ad_title_label),
        textFieldState = titleState,
        maxCharacters = 140,
        isInvalid = isInvalid,
        headerTrailing = {
            if (isInvalid) {
                ErrorPill()
            }
            CopyPill(value = titleState.text.toString())
            AiGeneratedBadge()
        },
    )
}

@Composable
private fun AdDescriptionCard(
    descriptionState: TextFieldState,
    isInvalid: Boolean,
) {
    PreviewSectionInputCard(
        label = stringResource(Res.string.ad_description_label),
        textFieldState = descriptionState,
        maxCharacters = 9000,
        isInvalid = isInvalid,
        headerTrailing = {
            if (isInvalid) {
                ErrorPill()
            }
            CopyPill(value = descriptionState.text.toString())
            AiGeneratedBadge()
        },
    )
}

@Composable
private fun AdPriceCard(
    priceTextFieldState: TextFieldState,
    minPrice: Float,
    maxPrice: Float,
) {
    PreviewSectionCard(label = stringResource(Res.string.ad_your_price)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)) {
            val price = remember(priceTextFieldState.text) {
                (priceTextFieldState.text.toString().toFloatOrNull() ?: ((maxPrice + minPrice) / 2f))
                    .coerceIn(minPrice, maxPrice)
            }

            val textStyle = AppTheme.typography.headline
            ProvideTextStyle(textStyle) {
                TextField(
                    state = priceTextFieldState,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    inputTransformation = DigitOnlyInputTransformation,
                    outputTransformation = ThousandSeparatorOutputTransformation,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.colors.surfaceLow,
                        unfocusedContainerColor = AppTheme.colors.surfaceLow,
                        focusedIndicatorColor = AppTheme.colors.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    prefix = {
                        // TODO: change currency (SIR-15)
                        Text(text = "₴", style = textStyle)
                    },
                )
            }

            CopyPill(
                modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                // TODO: change currency (SIR-15)
                value = "₴ ${formatPrice(price)}",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.Spacing.xl3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatPrice(minPrice),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.outline,
                )
                Slider(
                    modifier = Modifier.weight(1f),
                    value = price,
                    onValueChange = {
                        priceTextFieldState.setTextAndPlaceCursorAtEnd(it.fastRoundToInt().toString())
                    },
                    valueRange = minPrice..maxPrice,
                    colors = SliderDefaults.colors(
                        thumbColor = AppTheme.colors.primary,
                        activeTrackColor = AppTheme.colors.primary.copy(alpha = 0.24f),
                    ),
                )
                Text(
                    text = formatPrice(maxPrice),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.outline,
                )
            }

            Text(
                modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3),
                text = stringResource(
                    Res.string.ad_price_ai_estimated_range,
                    formatPrice(minPrice),
                    formatPrice(maxPrice),
                ),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onSurfaceMuted,
            )
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

@Composable
private fun ValidationError.toDisplayString(): String = when (this) {
    ValidationError.Required -> stringResource(Res.string.error_attr_required)
    ValidationError.MustBeNumeric -> stringResource(Res.string.error_attr_must_be_numeric)
    is ValidationError.BelowMinimum -> stringResource(Res.string.error_attr_below_minimum, min)
    is ValidationError.AboveMaximum -> stringResource(Res.string.error_attr_above_maximum, max)
    is ValidationError.InvalidSelection -> stringResource(Res.string.error_attr_invalid_selection)
    ValidationError.MultipleValuesNotAllowed -> stringResource(Res.string.error_attr_multiple_values_not_allowed)
}

// region Previews

@PreviewLightDark
@Composable
private fun ValidationBannerPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            ValidationBanner(
                errors = listOf(
                    "Title: at least 10 characters",
                    "Description: at least 30 characters",
                    "Select a category",
                    "Add your location",
                ),
                modifier = Modifier.padding(AppDimens.Spacing.xl3),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ValidationStatusCardValidPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            ValidationStatusCard(
                isValid = true,
                errorCount = 0,
                modifier = Modifier.padding(AppDimens.Spacing.xl3),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ValidationStatusCardInvalidPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            ValidationStatusCard(
                isValid = false,
                errorCount = 3,
                modifier = Modifier.padding(AppDimens.Spacing.xl3),
            )
        }
    }
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

@PreviewLightDark
@Composable
private fun AdPriceCardMinPreview() {
    AppTheme {
        Surface {
            AdPriceCard(
                priceTextFieldState = rememberTextFieldState("1000"),
                minPrice = 1000f,
                maxPrice = 50000f,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewAdEditableSectionsValidPreview() {
    PreviewAdEditableSectionsPreview(
        title = "Nike Air Max 90 Size 42",
        description = "Well-kept sneakers with minor wear on the outsole and clean upper panels.",
    )
}

@PreviewLightDark
@Composable
private fun AdPriceCardMidPreview() {
    AppTheme {
        Surface {
            AdPriceCard(
                priceTextFieldState = rememberTextFieldState("25500"),
                minPrice = 1000f,
                maxPrice = 50000f,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewAdEditableSectionsInvalidPreview() {
    PreviewAdEditableSectionsPreview(
        title = "Too short",
        description = "Needs more detail",
    )
}

@Composable
private fun PreviewAdEditableSectionsPreview(
    title: String,
    description: String,
) {
    AppTheme {
        Surface(
            color = AppTheme.colors.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.Spacing.xl3),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
            ) {
                AdTitleCard(
                    titleState = rememberTextFieldState(title),
                    isInvalid = title.trim().length < TitleMinLength,
                )
                AdDescriptionCard(
                    descriptionState = rememberTextFieldState(description),
                    isInvalid = description.trim().length < DescriptionMinLength,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AdPriceCardMaxPreview() {
    AppTheme {
        Surface {
            AdPriceCard(
                priceTextFieldState = rememberTextFieldState("50000"),
                minPrice = 1000f,
                maxPrice = 50000f,
            )
        }
    }
}

// endregion
