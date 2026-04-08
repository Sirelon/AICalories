package com.sirelon.aicalories.features.seller.auth.data

import com.sirelon.aicalories.features.seller.auth.data.response.PostAdvertRootResponse
import com.sirelon.aicalories.features.seller.auth.domain.OlxMeResponse
import com.sirelon.aicalories.features.seller.auth.domain.OlxUserResponse
import com.sirelon.aicalories.features.seller.categories.data.responses.OlxAttributeResponse
import com.sirelon.aicalories.features.seller.categories.data.responses.OlxAttributesResponse
import com.sirelon.aicalories.features.seller.categories.data.responses.OlxCategoriesRootResponse
import com.sirelon.aicalories.features.seller.categories.data.responses.OlxCategoryResponse
import com.sirelon.aicalories.features.seller.categories.data.responses.OlxCategorySuggestionResponse
import com.sirelon.aicalories.features.seller.location.data.response.OlxLocationResponse
import com.sirelon.aicalories.features.seller.location.data.response.OlxLocationsRootResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class OlxApiClient(
    private val httpClient: HttpClient,
) {
    suspend fun getAuthenticatedUser(): Result<OlxUserResponse> = runCatching {
        val response = httpClient.get("users/me")
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }

        response.body<OlxMeResponse>().user
    }

    suspend fun loadCategories(): List<OlxCategoryResponse> {
        val response = httpClient.get("categories")
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }
        return response.body<OlxCategoriesRootResponse>().data
    }

    suspend fun loadCategorySuggestionId(query: String): Int? {
        val response = httpClient.get("categories/suggestion") {
            parameter("q", query)
        }
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }
        return response.body<OlxCategorySuggestionResponse>().data.firstOrNull()?.id
    }

    internal suspend fun loadAttributes(categoryId: Int): List<OlxAttributeResponse> {
        val response = httpClient.get("categories/$categoryId/attributes")
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }
        return response.body<OlxAttributesResponse>().data ?: emptyList()
    }

    internal suspend fun getLocations(latitude: Double, longitude: Double): List<OlxLocationResponse> {
        val response = httpClient.get("locations") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
        }
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }
        return response.body<OlxLocationsRootResponse>().data
    }

    internal suspend fun postAdvert(request: PostAdvertRequest): Result<PostAdvertResult> = runCatching {
        val response = httpClient.post("adverts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }
        val body = response.body<PostAdvertRootResponse>()
        val advert = body.data ?: throw IllegalStateException("Missing advert data in OLX response")
        val advertId = advert.id ?: throw IllegalStateException("Missing advert id in OLX response")

        PostAdvertResult(
            id = advertId,
            status = advert.status ?: "",
            url = advert.url,
        )
    }
}
