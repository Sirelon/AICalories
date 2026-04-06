package com.sirelon.aicalories.features.seller.ad.preview_ad

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEffect
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEffect.ShowMessage
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.CategorySelected
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.FetchLocation
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.Publish
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdState
import com.sirelon.aicalories.features.seller.categories.data.CategoriesRepository
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.location.data.LocationRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PreviewAdViewModel(
    private val advertisement: Advertisement,
    private val categoriesRepository: CategoriesRepository,
    private val locationRepository: LocationRepository,
) : BaseViewModel<PreviewAdState, PreviewAdEvent, PreviewAdEffect>() {

    val titleState = TextFieldState(advertisement.title)
    val descriptionState = TextFieldState(advertisement.description)

    init {
        snapshotFlow { titleState.text }
            .distinctUntilChanged()
            .debounce(300L)
            .flatMapLatest {
                categoriesRepository.categorySuggestion(it.toString())
            }
            .catch {
                it.printStackTrace()
            }
            .onEach {
                updateSelectedCategory(category = it)
            }
            .launchIn(viewModelScope)
    }

    override fun initialState() = PreviewAdState(
        categoryLabel = "",
        price = advertisement.suggestedPrice,
        minPrice = advertisement.minPrice,
        maxPrice = advertisement.maxPrice,
        images = advertisement.images,
    )

    override fun onEvent(event: PreviewAdEvent) {
        when (event) {
            is CategorySelected -> viewModelScope.launch {
                updateSelectedCategory(event.category)
            }

            is PreviewAdEvent.OnPriceChanged -> {
                setState {
                    it.copy(
                        price = event.price,
                        maxPrice = it.maxPrice.coerceAtLeast(event.price),
                        minPrice = it.minPrice.coerceAtMost(event.price),
                    )
                }
            }

            FetchLocation -> viewModelScope.launch {
                fetchLocation()
            }

            PreviewAdEvent.OnChangeCategoryClick -> postEffect(PreviewAdEffect.GoToGategoryPicker)

            Publish -> {
                // TODO: Post to OLX API — read titleState.text, descriptionState.text, selectedPrice here
                postEffect(ShowMessage("Publishing not yet implemented"))
            }
        }
    }

    private suspend fun fetchLocation() {
        setState { it.copy(locationLoading = true) }
        try {
            val location = locationRepository.fetchUserLocation()
            setState { it.copy(location = location, locationLoading = false) }
        } catch (e: Exception) {
            e.printStackTrace()
            setState { it.copy(locationLoading = false) }
        }
    }

    private suspend fun updateSelectedCategory(category: OlxCategory) {
        val path = mutableListOf(category.label)
        var parentId = category.parentId
        while (parentId != null) {
            val parent = categoriesRepository.getCategoryById(parentId) ?: break
            path.add(0, parent.label)
            parentId = parent.parentId
        }
        setState { it.copy(categoryLabel = path.joinToString(" / ")) }
    }
}
