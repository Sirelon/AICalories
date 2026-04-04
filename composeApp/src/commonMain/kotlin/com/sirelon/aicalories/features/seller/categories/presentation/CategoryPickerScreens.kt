package com.sirelon.aicalories.features.seller.categories.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppScaffold
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.screens.LoadingOverlay
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
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(AppDimens.Spacing.xl3),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl),
            ) {
                items(state.categories) { category ->
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
        LoadingOverlay(isLoading = state.isLoading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(state.categories) { category ->
                    SubcategoryRow(
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
private fun CategoryGridCard(category: CategoryWithChildCount, onClick: () -> Unit) {
    val icon = categoryIconPainter(category.category.id)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.aspectRatio(1f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppDimens.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = icon ?: painterResource(Res.drawable.ic_layout_grid),
                contentDescription = null,
                modifier = Modifier.size(AppDimens.Size.xl6),
                tint = AppTheme.colors.primary,
            )
            Text(
                text = category.category.label,
                style = AppTheme.typography.label,
                textAlign = TextAlign.Center,
                maxLines = 2,
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
        headlineContent = { Text(category.category.label) },
        supportingContent = if (category.childCount > 0) {
            {
                Text(stringResource(Res.string.subcategory_count, category.childCount))
            }
        } else {
            null
        },
        trailingContent = {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right),
                contentDescription = null,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
    HorizontalDivider()
}
