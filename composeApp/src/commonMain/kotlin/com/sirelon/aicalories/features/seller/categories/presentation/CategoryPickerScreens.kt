package com.sirelon.aicalories.features.seller.categories.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.designsystem.screens.LoadingOverlay
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerEvent.NavigateTo
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerEvent.NavigateToIndex
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerEvent.Reset
import com.sirelon.aicalories.features.seller.categories.presentation.CategoryPickerContract.CategoryPickerEvent.Search
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.ic_chevron_right
import com.sirelon.aicalories.generated.resources.ic_circle_check_big
import com.sirelon.aicalories.generated.resources.ic_x
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryPickerSheet(
    onDismiss: () -> Unit,
    onCategorySelected: (OlxCategory) -> Unit,
) {
    val viewModel: CategoryPickerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val c = AppTheme.colors
    val t = AppTheme.typography

    Column(modifier = Modifier.fillMaxHeight(0.92f)) {
        // ── Search bar ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .padding(horizontal = AppDimens.Spacing.xl5, vertical = AppDimens.Spacing.xl)
                .fillMaxWidth()
                .background(c.surfaceLow, RoundedCornerShape(AppDimens.BorderRadius.xl))
                .padding(horizontal = AppDimens.Spacing.xl, vertical = AppDimens.Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = c.onSurfaceMuted,
                modifier = Modifier.size(AppDimens.Size.xl3),
            )
            BasicTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(Search(it)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = t.body.copy(color = c.onSurface),
                cursorBrush = SolidColor(c.primary),
                decorationBox = { inner ->
                    Box {
                        if (state.searchQuery.isEmpty()) {
                            Text("Пошук категорії", style = t.body, color = c.onSurfaceMuted)
                        }
                        inner()
                    }
                },
            )
            if (state.searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.onEvent(Search("")) },
                    modifier = Modifier.size(AppDimens.Size.xl5),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_x),
                        contentDescription = null,
                        tint = c.onSurfaceMuted,
                        modifier = Modifier.size(AppDimens.Size.xl2),
                    )
                }
            }
        }

        // ── Breadcrumbs ───────────────────────────────────────────
        if (state.searchQuery.isEmpty() && state.path.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = AppDimens.Spacing.xl5)
                    .padding(bottom = AppDimens.Spacing.xl),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.s),
            ) {
                TextButton(
                    onClick = { viewModel.onEvent(NavigateToIndex(0)) },
                    contentPadding = PaddingValues(AppDimens.Spacing.xs4),
                ) {
                    Text(
                        "Усі",
                        style = t.body.copy(fontWeight = FontWeight.SemiBold),
                        color = c.primary,
                    )
                }
                state.path.forEachIndexed { index, category ->
                    Text(
                        "›",
                        color = c.onSurface.copy(alpha = 0.34f),
                        fontSize = AppDimens.TextSize.xl,
                    )
                    val isLast = index == state.path.lastIndex
                    TextButton(
                        onClick = { viewModel.onEvent(NavigateToIndex(index)) },
                        contentPadding = PaddingValues(AppDimens.Spacing.xs4),
                    ) {
                        Text(
                            category.label,
                            style = t.body.copy(
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = if (isLast) c.onSurface else c.primary,
                        )
                    }
                }
            }
        }

        // ── Category list ─────────────────────────────────────────
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (state.isLoading) {
                item {
                    LoadingOverlay(isLoading = true) {}
                }
            } else {
                val isSearchMode = state.searchQuery.isNotBlank()
                itemsIndexed(state.displayItems, key = { _, item -> item.category.id }) { index, item ->
                    val category = item.category
                    if (isSearchMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(category); onDismiss() }
                                .padding(
                                    horizontal = AppDimens.Spacing.xl5,
                                    vertical = AppDimens.Spacing.xl,
                                ),
                        ) {
                            Text(
                                category.label,
                                style = t.body.copy(fontWeight = FontWeight.Medium),
                                color = c.onSurface,
                            )
                            if (item.parentChain.isNotEmpty()) {
                                Text(item.parentChain, style = t.caption, color = c.onSurfaceMuted)
                            }
                        }
                    } else {
                        val isActive = state.path.lastOrNull()?.id == category.id
                        val icon = categoryIconPainter(category.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isActive) c.primary.copy(alpha = 0.06f)
                                    else Color.Transparent,
                                )
                                .clickable {
                                    if (category.isLeaf) {
                                        onCategorySelected(category)
                                        onDismiss()
                                    } else {
                                        viewModel.onEvent(NavigateTo(category))
                                    }
                                }
                                .padding(
                                    horizontal = AppDimens.Spacing.xl5,
                                    vertical = AppDimens.Spacing.xl2,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
                        ) {
                            if (icon != null) {
                                Box(
                                    modifier = Modifier
                                        .size(AppDimens.Size.xl9)
                                        .background(
                                            c.surfaceLow,
                                            RoundedCornerShape(AppDimens.BorderRadius.l),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(AppDimens.Size.xl5),
                                        tint = c.onSurface,
                                    )
                                }
                            }
                            Text(
                                category.label,
                                modifier = Modifier.weight(1f),
                                style = t.body.copy(
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (isActive) c.primary else c.onSurface,
                            )
                            when {
                                category.isLeaf && isActive -> Text(
                                    "ОБРАНО",
                                    style = t.caption.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = c.primary,
                                    ),
                                )
                                category.isLeaf -> Text(
                                    "ОБРАТИ",
                                    style = t.caption.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = c.success,
                                    ),
                                )
                                isActive -> Icon(
                                    painter = painterResource(Res.drawable.ic_circle_check_big),
                                    contentDescription = null,
                                    tint = c.primary,
                                    modifier = Modifier.size(AppDimens.Size.xl3),
                                )
                                else -> Icon(
                                    painter = painterResource(Res.drawable.ic_chevron_right),
                                    contentDescription = null,
                                    tint = c.onSurface.copy(alpha = 0.34f),
                                    modifier = Modifier.size(AppDimens.Size.xl2),
                                )
                            }
                        }
                    }
                    if (index < state.displayItems.lastIndex) {
                        HorizontalDivider(color = c.outlineVariant.copy(alpha = 0.13f))
                    }
                }
            }
        }

        // ── Footer ────────────────────────────────────────────────
        HorizontalDivider(color = c.outlineVariant.copy(alpha = 0.2f))
        Row(
            modifier = Modifier
                .padding(horizontal = AppDimens.Spacing.xl5, vertical = AppDimens.Spacing.xl)
                .navigationBarsPadding()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppButton(
                text = "Скинути",
                onClick = { viewModel.onEvent(Reset) },
                style = AppButtonDefaults.secondary(),
                modifier = Modifier.wrapContentWidth(),
            )
            val selectedLabel = state.path.lastOrNull()?.label
            AppButton(
                text = if (selectedLabel != null) "Обрати · $selectedLabel" else "Обрати",
                onClick = {
                    state.path.lastOrNull()?.let { onCategorySelected(it); onDismiss() }
                },
                enabled = state.path.isNotEmpty(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
