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
import com.sirelon.aicalories.features.seller.categories.domain.AttributeInputType
import com.sirelon.aicalories.features.seller.categories.domain.AttributeValidationResult
import com.sirelon.aicalories.features.seller.categories.domain.AttributeValidator
import com.sirelon.aicalories.features.seller.categories.domain.OlxCategory
import com.sirelon.aicalories.features.seller.location.data.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PreviewAdViewModel(
    private val advertisement: Advertisement,
    private val categoriesRepository: CategoriesRepository,
    private val locationRepository: LocationRepository,
    private val attributeValidator: AttributeValidator,
) : BaseViewModel<PreviewAdState, PreviewAdEvent, PreviewAdEffect>() {

    val titleState = TextFieldState(advertisement.title)
    val descriptionState = TextFieldState(advertisement.description)

    private val selectedCategoryId = MutableStateFlow<Int?>(null)

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

        selectedCategoryId
            .filterNotNull()
            .flatMapLatest { categoryId ->
                categoriesRepository.getAttributes(categoryId)
            }
            .onEach { attributes ->
                setState { it.copy(attributeItems = attributes.map { OlxAttributeState(it) }) }
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

            is PreviewAdEvent.AttributeValueChanged -> setState { currentState ->
                val index = currentState.attributeItems.indexOfFirst { it.attribute.code == event.attributeCode }
                if (index == -1) return@setState currentState
                val item = currentState.attributeItems[index]
                val valuesToValidate = when (item.attribute.inputType) {
                    AttributeInputType.SingleSelect, AttributeInputType.MultiSelect ->
                        event.values.map { it.code }
                    AttributeInputType.NumericInput, AttributeInputType.TextInput ->
                        event.values.map { it.label }
                }
                val error = (attributeValidator.validate(item.attribute, valuesToValidate) as? AttributeValidationResult.Invalid)?.reason
                val updatedItems = currentState.attributeItems.toMutableList()
                updatedItems[index] = item.copy(selectedValues = event.values, error = error)
                currentState.copy(attributeItems = updatedItems)
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
        setState { it.copy(categoryLabel = path.joinToString(" / "), attributeItems = emptyList()) }
        selectedCategoryId.value = category.id
    }
}
