package com.sirelon.aicalories.features.attributes.data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

// Deserializes both String and Int JSON values as a plain String.
// The OLX API returns value codes as either "used" (string) or 2062 (int).
internal object StringOrIntAsStringSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("StringOrInt", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeString()
        val element = jsonDecoder.decodeJsonElement() as? JsonPrimitive ?: return ""
        return element.content
    }
}

@Serializable
data class OlxAttributesResponseDto(
    @SerialName("data") val data: List<OlxAttributeDto>,
)

@Serializable
data class OlxAttributeDto(
    @SerialName("code") val code: String,
    @SerialName("label") val label: String,
    @SerialName("unit") val unit: String = "",
    @SerialName("validation") val validation: OlxAttributeValidationDto,
    @SerialName("values") val values: List<OlxAttributeValueDto> = emptyList(),
)

@Serializable
data class OlxAttributeValidationDto(
    @SerialName("type") val type: String,
    @SerialName("required") val required: Boolean,
    @SerialName("numeric") val numeric: Boolean,
    @SerialName("min") val min: Double? = null,
    @SerialName("max") val max: Double? = null,
    @SerialName("allow_multiple_values") val allowMultipleValues: Boolean,
)

@Serializable
data class OlxAttributeValueDto(
    @SerialName("code") @Serializable(with = StringOrIntAsStringSerializer::class) val code: String,
    @SerialName("label") val label: String,
)
