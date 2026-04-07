package com.sirelon.aicalories.features.seller.categories.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.screens.LoadingOverlay
import com.sirelon.aicalories.designsystem.utils.contrastColor
import com.sirelon.aicalories.designsystem.utils.generateRandomColor
import com.sirelon.aicalories.designsystem.utils.opposite
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.ic_chevron_left
import com.sirelon.aicalories.generated.resources.ic_chevron_right
import com.sirelon.aicalories.generated.resources.ic_layout_grid
import com.sirelon.aicalories.generated.resources.select_category
import com.sirelon.aicalories.generated.resources.subcategory_count
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SelectRootCategoryScreen(
    onBack: () -> Unit,
    onCategorySelected: (OlxCategory) -> Unit,
    onNavigateToSubcategory: (OlxCategory) -> Unit,
) {
    val viewModel: CategoryPickerViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.select_category)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_chevron_left),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LoadingOverlay(isLoading = state.isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(AppDimens.Spacing.xl3),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
            ) {
                items(
                    items = state.categories,
                    key = { it.category.id }
                ) { category ->
                    CategoryGridCard(
                        category = category,
                        onClick = {
                            if (category.category.isLeaf) onCategorySelected(category.category)
                            else onNavigateToSubcategory(category.category)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun SelectSubcategoryScreen(
    category: OlxCategory,
    onBack: () -> Unit,
    onCategorySelected: (OlxCategory) -> Unit,
    onNavigateToSubcategory: (OlxCategory) -> Unit,
) {
    val viewModel: CategoryPickerViewModel = koinViewModel(
        key = category.id.toString(),
        parameters = { parametersOf(category) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppScaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_chevron_left),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        val lastItem = remember(state.categories) { state.categories.lastOrNull() }
        Box(modifier = Modifier.padding(padding).padding(vertical = AppDimens.Spacing.xl3)) {
            LoadingOverlay(isLoading = state.isLoading) {
                ElevatedCard(modifier = Modifier.padding(horizontal = AppDimens.Spacing.xl3)) {
                    LazyColumn {
                        items(
                            items = state.categories,
                            key = { it.category.id },
                        ) { category ->
                            SubcategoryRow(
                                category = category,
                                onClick = {
                                    if (category.category.isLeaf) onCategorySelected(category.category)
                                    else onNavigateToSubcategory(category.category)
                                },
                            )
                            if (lastItem != category) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGridCard(category: CategoryWithChildCount, onClick: () -> Unit) {
    val icon = categoryIconPainter(category.category.id)
    val color = generateRandomColor(category.category.label)
    ElevatedCard(
        modifier = Modifier.aspectRatio(1f),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = color,
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppDimens.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val bgIconColor = remember(color) {
                color.opposite()
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = bgIconColor,
                )
            ) {
                Box(
                    modifier = Modifier.padding(AppDimens.Spacing.xl3),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon ?: painterResource(Res.drawable.ic_layout_grid),
                        contentDescription = null,
                        modifier = Modifier.size(AppDimens.Size.xl6),
                        tint = bgIconColor.contrastColor(),
                    )
                }
            }
            Text(
                text = category.category.label,
                style = AppTheme.typography.label,
                color = color.contrastColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimens.Spacing.m),
            )
            if (category.childCount > 0) {
                Text(
                    text = stringResource(Res.string.subcategory_count, category.childCount),
                    style = AppTheme.typography.caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppDimens.Spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun SubcategoryRow(category: CategoryWithChildCount, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        content = { Text(category.category.label) },
        supportingContent = if (category.childCount > 0) {
            {
                Text(stringResource(Res.string.subcategory_count, category.childCount))
            }
        } else {
            null
        },
        trailingContent = {
            if (!category.category.isLeaf) {
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_right),
                    contentDescription = null,
                )
            }
        },
    )
}
