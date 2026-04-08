package com.sirelon.aicalories.features.seller.auth.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class PostAdvertRootResponse(
    @SerialName("data")
    val data: PostAdvertDataResponse?,
)

@Serializable
internal class PostAdvertDataResponse(
    @SerialName("id")
    val id: Long?,

    @SerialName("status")
    val status: String?,

    @SerialName("url")
    val url: String?,
)
