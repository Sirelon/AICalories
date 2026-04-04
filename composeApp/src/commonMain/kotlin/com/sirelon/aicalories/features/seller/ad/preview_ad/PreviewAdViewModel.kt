package com.sirelon.aicalories.features.seller.ad.preview_ad

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import com.sirelon.aicalories.features.common.presentation.BaseViewModel
import com.sirelon.aicalories.features.seller.ad.Advertisement
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEffect
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.CategoryChanged
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.DismissCategoryPicker
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.Publish
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdEvent.ShowCategoryPicker
import com.sirelon.aicalories.features.seller.ad.preview_ad.PreviewAdContract.PreviewAdState
import com.sirelon.aicalories.features.seller.categories.data.CategoriesRepository

class PreviewAdViewModel(
    private val advertisement: Advertisement,
    private val categoriesRepository: CategoriesRepository,
) : BaseViewModel<PreviewAdState, PreviewAdEvent, PreviewAdEffect>() {

    val titleState = TextFieldState(advertisement.title)
    val descriptionState = TextFieldState(advertisement.description)
    var selectedPrice by mutableFloatStateOf(advertisement.suggestedPrice.toFloat())

    override fun initialState() = PreviewAdState(
        category = advertisement.category,
        minPrice = advertisement.minPrice.toFloat(),
        maxPrice = advertisement.maxPrice.toFloat(),
        originalPrice = advertisement.suggestedPrice,
        images = advertisement.images,
    )

    override fun onEvent(event: PreviewAdEvent) = when (event) {
        is CategoryChanged -> setState { it.copy(category = event.category, isCategoryPickerVisible = false) }
        ShowCategoryPicker -> setState { it.copy(isCategoryPickerVisible = true) }
        DismissCategoryPicker -> setState { it.copy(isCategoryPickerVisible = false) }
        Publish -> {
            // TODO: Post to OLX API — read titleState.text, descriptionState.text, selectedPrice here
            postEffect(PreviewAdEffect.ShowMessage("Publishing not yet implemented"))
        }
    }
}
