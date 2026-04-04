package com.sirelon.aicalories.features.attributes.presentation

import com.sirelon.aicalories.features.attributes.domain.OlxAttribute

interface AttributesContract {

    data class AttributesState(
        val isLoading: Boolean = true,
        val attributes: List<OlxAttribute> = emptyList(),
        val errorMessage: String? = null,
    )

    sealed interface AttributesEvent {
        data object Retry : AttributesEvent
    }
}
