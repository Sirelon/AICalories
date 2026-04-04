package com.sirelon.aicalories.features.attributes.data

import com.sirelon.aicalories.features.attributes.data.dto.OlxAttributeDto
import com.sirelon.aicalories.features.attributes.data.dto.OlxAttributeValueDto
import com.sirelon.aicalories.features.attributes.domain.AttributeInputType
import com.sirelon.aicalories.features.attributes.domain.AttributeValidationRules
import com.sirelon.aicalories.features.attributes.domain.OlxAttribute
import com.sirelon.aicalories.features.attributes.domain.OlxAttributeValue

class AttributeMapper {

    fun mapToDomain(dtos: List<OlxAttributeDto>): List<OlxAttribute> = dtos.map { mapAttribute(it) }

    private fun mapAttribute(dto: OlxAttributeDto): OlxAttribute {
        val rules = AttributeValidationRules(
            required = dto.validation.required,
            numeric = dto.validation.numeric,
            min = dto.validation.min,
            max = dto.validation.max,
            allowMultipleValues = dto.validation.allowMultipleValues,
        )
        return OlxAttribute(
            code = dto.code,
            label = dto.label,
            unit = dto.unit,
            inputType = deriveInputType(dto),
            validationRules = rules,
            allowedValues = dto.values.map { mapValue(it) },
        )
    }

    private fun deriveInputType(dto: OlxAttributeDto): AttributeInputType = when {
        dto.values.isNotEmpty() && dto.validation.allowMultipleValues -> AttributeInputType.MultiSelect
        dto.values.isNotEmpty() -> AttributeInputType.SingleSelect
        dto.validation.numeric -> AttributeInputType.NumericInput
        else -> AttributeInputType.TextInput
    }

    private fun mapValue(dto: OlxAttributeValueDto) = OlxAttributeValue(
        code = dto.code,
        label = dto.label,
    )
}
