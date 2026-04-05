package com.sirelon.aicalories.features.seller.ad

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sirelon.aicalories.features.seller.ad.generate_ad.GenerateAdScreen
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdScreen
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.categories.presentation.SelectRootCategoryScreen
import com.sirelon.aicalories.features.seller.categories.presentation.SelectSubcategoryScreen
import kotlinx.serialization.Serializable


sealed interface AdDestination {

    @Serializable
    data object GenerateAd : AdDestination

    @Serializable
    data class PreviewAd(val advertisement: Advertisement) : AdDestination

    @Serializable
    data object SelectRootCategory : AdDestination

    @Serializable
    data class SelectSubcategory(val category: OlxCategory) : AdDestination
}

@Composable
fun AdRootScreen(onExit: () -> Unit) {

    val navBackStack = remember {
        mutableStateListOf<AdDestination>(AdDestination.GenerateAd)
    }

    var pendingCategory by remember { mutableStateOf<OlxCategory?>(null) }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AdDestination>()),
        entryProvider = entryProvider {
            entry<AdDestination.GenerateAd> {
                GenerateAdScreen(
                    onBack = onExit,
                    openAdPreview = { navBackStack.add(AdDestination.PreviewAd(it)) },
                )
            }

            entry<AdDestination.PreviewAd> { destination ->
                PreviewAdScreen(
                    advertisement = destination.advertisement,
                    onChangeCategoryClick = { navBackStack.add(AdDestination.SelectRootCategory) },
                    pendingCategory = pendingCategory,
                    onCategoryConsumed = { pendingCategory = null },
                )
            }

            entry<AdDestination.SelectRootCategory> {
                SelectRootCategoryScreen(
                    onBack = { navBackStack.removeAt(navBackStack.lastIndex) },
                    onCategorySelected = { category ->
                        while (navBackStack.last() !is AdDestination.PreviewAd) {
                            navBackStack.removeAt(navBackStack.lastIndex)
                        }
                        pendingCategory = category
                    },
                    onNavigateToSubcategory = { category ->
                        navBackStack.add(AdDestination.SelectSubcategory(category))
                    },
                )
            }

            entry<AdDestination.SelectSubcategory> { destination ->
                SelectSubcategoryScreen(
                    category = destination.category,
                    onBack = { navBackStack.removeAt(navBackStack.lastIndex) },
                    onCategorySelected = { category ->
                        while (navBackStack.last() !is AdDestination.PreviewAd) {
                            navBackStack.removeAt(navBackStack.lastIndex)
                        }
                        pendingCategory = category
                    },
                    onNavigateToSubcategory = { category ->
                        navBackStack.add(AdDestination.SelectSubcategory(category))
                    },
                )
            }
        },
    )
}
