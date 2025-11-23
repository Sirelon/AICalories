package com.sirelon.aicalories

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.mohamedrejeb.calf.picker.coil.KmpFileFetcher
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.di.appModule
import com.sirelon.aicalories.di.networkModule
import com.sirelon.aicalories.features.agile.AgileRoot
import com.sirelon.aicalories.features.analyze.ui.AnalyzeScreen
import com.sirelon.aicalories.features.history.ui.HistoryScreenRoute
import com.sirelon.aicalories.navigation.AppDestination
import org.koin.compose.KoinApplication

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
        application = { modules(appModule, networkModule) },
    ) {
        AppTheme {
            val navBackStack = remember {
                val startDestination = AppDestination.Agile

                mutableStateListOf<AppDestination>(startDestination)
            }

            val popDestination: () -> Unit = {
                if (navBackStack.size > 1) {
                    navBackStack.removeLastOrNull()
                }
            }
            val showBackButton: Boolean = navBackStack.size > 1

            fun navigateTo(destination: AppDestination) {
                if (navBackStack.lastOrNull() != destination) {
                    navBackStack.add(destination)
                }
            }

            fun popToAnalyze() {
                while (navBackStack.size > 1 && navBackStack.last() !is AppDestination.Analyze) {
                    navBackStack.removeLast()
                }
            }

            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = navBackStack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<AppDestination>()),
                entryProvider = entryProvider<AppDestination> {
                    entry<AppDestination.Agile> {
                        AgileRoot(onExit = popDestination)
                    }

                    entry<AppDestination.Analyze> {
                        AnalyzeScreen(
                            onBack = if (showBackButton) popDestination else null,
                            onResultConfirmed = { navigateTo(AppDestination.History) },
                        )
                    }
                    entry<AppDestination.History> {
                        HistoryScreenRoute(
                            onBack = popDestination.takeIf { showBackButton },
                            onCaptureNewMeal = { popToAnalyze() },
                        )
                    }
                },
            )
        }
    }
}
