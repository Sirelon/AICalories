package com.sirelon.aicalories.features.seller.ad

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sirelon.aicalories.features.seller.ad.generate_ad.GenerateAdScreen
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdScreen
import kotlinx.serialization.Serializable


sealed interface AdDestination {

    @Serializable
    data object GenerateAd : AdDestination

    @Serializable
    data class PreviewAd(val advertisement: Advertisement) : AdDestination

}

@Composable
fun AdRootScreen(onExit: () -> Unit) {

    val navBackStack = remember {
        mutableStateListOf<AdDestination>(AdDestination.GenerateAd)
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        // Forward navigation animation
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        // Back navigation animation
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AdDestination>()),
        entryProvider = entryProvider {
            entry<AdDestination.PreviewAd> {
                PreviewAdScreen(it.advertisement)
            }

            entry<AdDestination.GenerateAd> {
                GenerateAdScreen(
                    onBack = onExit,
                    openAdPreview = { navBackStack.add(AdDestination.PreviewAd(it)) },
                )
            }
        },
    )
}