package com.sirelon.aicalories.features.seller.ad.publish_success

import kotlinx.serialization.Serializable

@Serializable
data class PublishSuccessData(
    val url: String,
    val title: String,
    val priceFormatted: String,
    val primaryImageUrl: String?,
    val totalElapsedMs: Long,
)
