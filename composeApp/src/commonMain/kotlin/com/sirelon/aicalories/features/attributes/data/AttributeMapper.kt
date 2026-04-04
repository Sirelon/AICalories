package com.sirelon.aicalories.features.attributes.data

import com.sirelon.aicalories.features.attributes.data.response.OlxAttributeResponse
import com.sirelon.aicalories.features.attributes.data.response.OlxAttributeValidationResponse
import com.sirelon.aicalories.features.attributes.data.response.OlxAttributeValueResponse
import com.sirelon.aicalories.features.attributes.domain.AttributeInputType
import com.sirelon.aicalories.features.attributes.domain.AttributeValidationRules
import com.sirelon.aicalories.features.attributes.domain.OlxAttribute
import com.sirelon.aicalories.features.attributes.domain.OlxAttributeValue

class AttributeMapper {

    internal fun mapToDomain(responses: List<OlxAttributeResponse>): List<OlxAttribute> =
        responses.mapNotNull { mapAttribute(it) }

    private fun mapAttribute(response: OlxAttributeResponse): OlxAttribute? {
        // code is the primary key — skip the attribute entirely if missing
        val code = response.code ?: return null
        val rules = mapValidationRules(response.validation)
        val allowedValues = response.values?.mapNotNull { mapValue(it) } ?: emptyList()
        return OlxAttribute(
            code = code,
            label = response.label ?: "",
            unit = response.unit ?: "",
            inputType = deriveInputType(allowedValues, rules),
            validationRules = rules,
            allowedValues = allowedValues,
        )
    }

    private fun mapValidationRules(response: OlxAttributeValidationResponse?): AttributeValidationRules =
        AttributeValidationRules(
            required = response?.required ?: false,
            numeric = response?.numeric ?: false,
            min = response?.min,
            max = response?.max,
            allowMultipleValues = response?.allowMultipleValues ?: false,
        )

    private fun deriveInputType(
        allowedValues: List<OlxAttributeValue>,
        rules: AttributeValidationRules,
    ): AttributeInputType = when {
        allowedValues.isNotEmpty() && rules.allowMultipleValues -> AttributeInputType.MultiSelect
        allowedValues.isNotEmpty() -> AttributeInputType.SingleSelect
        rules.numeric -> AttributeInputType.NumericInput
        else -> AttributeInputType.TextInput
    }

    private fun mapValue(response: OlxAttributeValueResponse): OlxAttributeValue? {
        val code = response.code ?: return null
        return OlxAttributeValue(
            code = code,
            label = response.label ?: "",
        )
    }
}
