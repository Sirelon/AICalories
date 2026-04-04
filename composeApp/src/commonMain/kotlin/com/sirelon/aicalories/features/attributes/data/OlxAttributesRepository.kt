package com.sirelon.aicalories.features.attributes.data

import com.sirelon.aicalories.features.attributes.data.dto.OlxAttributesResponseDto
import com.sirelon.aicalories.features.attributes.domain.OlxAttribute
import com.sirelon.aicalories.features.seller.auth.data.OlxRemoteErrorParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OlxAttributesRepository(
    private val httpClient: HttpClient,
    private val mapper: AttributeMapper,
) {
    companion object {
        const val HARDCODED_CATEGORY_ID = 4  // Mobile phones
    }

    fun getAttributes(categoryId: Int = HARDCODED_CATEGORY_ID): Flow<List<OlxAttribute>> = flow {
        val response = httpClient.get("categories/$categoryId/attributes")
        if (!response.status.isSuccess()) {
            throw OlxRemoteErrorParser.parse(response.status, response.bodyAsText())
        }
        val dtos = response.body<OlxAttributesResponseDto>().data
        emit(mapper.mapToDomain(dtos))
    }
}
