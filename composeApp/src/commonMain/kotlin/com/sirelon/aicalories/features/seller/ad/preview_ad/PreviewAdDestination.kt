package com.sirelon.aicalories.features.seller.ad.preview_ad

import kotlinx.serialization.Serializable

internal sealed interface PreviewAdDestination {
    @Serializable
    data object Content : PreviewAdDestination

    @Serializable
    data object PublishConfirm : PreviewAdDestination

    @Serializable
    data object Publishing : PreviewAdDestination
}