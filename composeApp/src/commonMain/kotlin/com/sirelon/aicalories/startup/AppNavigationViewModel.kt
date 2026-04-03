package com.sirelon.aicalories.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthRepository
import com.sirelon.aicalories.navigation.AppDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppNavigationViewModel(
    private val authRepository: OlxAuthRepository,
    private val startupStore: AppStartupStore,
) : ViewModel() {

    private val _backStack = MutableStateFlow<List<AppDestination>>(listOf(AppDestination.Splash))
    val backStack: StateFlow<List<AppDestination>> = _backStack.asStateFlow()

    init {
        viewModelScope.launch { resolveStartupDestination() }
    }

    fun navigateTo(destination: AppDestination) {
        val current = _backStack.value
        if (current.lastOrNull() != destination) {
            _backStack.value = current + destination
        }
    }

    fun popDestination() {
        val current = _backStack.value
        if (current.size > 1) {
            _backStack.value = current.dropLast(1)
        }
    }

    fun popToAnalyze() {
        val current = _backStack.value
        val index = current.indexOfLast { it is AppDestination.Analyze }
        if (index >= 0) {
            _backStack.value = current.subList(0, index + 1)
        }
    }

    private suspend fun resolveStartupDestination() {
        val initial: AppDestination = if (!startupStore.hasSeenOnboarding()) {
            startupStore.markOnboardingSeen()
            AppDestination.SellerOnboarding
        } else {
            val session = authRepository.currentSession()
            if (session.isAuthorized) AppDestination.Seller else AppDestination.SellerLanding
        }
        _backStack.value = listOf(initial)
    }
}
