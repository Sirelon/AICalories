package com.sirelon.aicalories

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.mohamedrejeb.calf.picker.coil.KmpFileFetcher
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.screens.LoadingOverlay
import com.sirelon.aicalories.di.appModule
import com.sirelon.aicalories.di.networkModule
import com.sirelon.aicalories.features.agile.AgileRoot
import com.sirelon.aicalories.features.analyze.ui.AnalyzeScreen
import com.sirelon.aicalories.features.datagenerator.ui.DataGeneratorScreen
import com.sirelon.aicalories.features.history.ui.HistoryScreenRoute
import com.sirelon.aicalories.features.seller.ad.AdRootScreen
import com.sirelon.aicalories.features.seller.ad.publish_success.PublishSuccessScreen
import com.sirelon.aicalories.features.seller.auth.presentation.SellerLandingScreenRoute
import com.sirelon.aicalories.features.seller.onboarding.OnboardingScreen
import com.sirelon.aicalories.navigation.AppDestination
import com.sirelon.aicalories.platform.openUrl
import com.sirelon.aicalories.startup.AppNavigationViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory {
        ImageLoader.Builder(it)
            .components {
                add(KmpFileFetcher.Factory())
            }
            .build()
    }

    KoinApplication(
        configuration = koinConfiguration {
            modules(appModule, networkModule)
        },
    ) {
        AppTheme {
            val navVm: AppNavigationViewModel = koinViewModel()
            val backStackList by navVm.backStack.collectAsStateWithLifecycle()

            // Single stable SnapshotStateList kept in sync with the VM's backstack
            val navBackStack = remember { mutableStateListOf<AppDestination>(AppDestination.Splash) }
            LaunchedEffect(backStackList) {
                if (navBackStack.toList() != backStackList) {
                    navBackStack.clear()
                    navBackStack.addAll(backStackList)
                }
            }

            val showBackButton: Boolean = navBackStack.size > 1

            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = navBackStack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AppDestination>()),
                entryProvider = entryProvider<AppDestination> {

                    entry<AppDestination.Splash> {
                        LoadingOverlay(isLoading = true) {}
                    }

                    entry<AppDestination.SellerOnboarding> {
                        OnboardingScreen {
                            navVm.replaceWith(AppDestination.SellerLanding)
                        }
                    }

                    entry<AppDestination.SellerLanding> {
                        SellerLandingScreenRoute(
                            openHome = { navVm.navigateTo(AppDestination.Seller) },
                        )
                    }

                    entry<AppDestination.Seller> {
                        AdRootScreen(
                            onExit = navVm::popDestination,
                            onConnectOlxClick = navVm::exitGuestModeToLanding,
                            onLogout = { navVm.replaceWith(AppDestination.SellerLanding) },
                            onPublishSuccess = navVm::navigateToPublishSuccess,
                        )
                    }

                    entry<AppDestination.SellerPublishSuccess> { destination ->
                        PublishSuccessScreen(
                            data = destination.data,
                            onViewOnOlx = { openUrl(destination.data.url) },
                            onCreateAnother = navVm::popToAdRoot,
                        )
                    }

                    entry<AppDestination.Agile> {
                        AgileRoot(
                            onExit = navVm::popDestination,
                            onOpenDataGenerator = { navVm.navigateTo(AppDestination.DataGenerator) }
                        )
                    }

                    entry<AppDestination.Analyze> {
                        AnalyzeScreen(
                            onBack = if (showBackButton) navVm::popDestination else null,
                            onResultConfirmed = { navVm.navigateTo(AppDestination.History) },
                        )
                    }

                    entry<AppDestination.History> {
                        HistoryScreenRoute(
                            onBack = navVm::popDestination.takeIf { showBackButton },
                            onCaptureNewMeal = { navVm.popToAnalyze() },
                        )
                    }

                    entry<AppDestination.DataGenerator> {
                        DataGeneratorScreen(onBack = navVm::popDestination.takeIf { showBackButton })
                    }
                },
            )
        }
    }
}
