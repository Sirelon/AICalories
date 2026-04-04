package com.sirelon.aicalories.features.seller.categories.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sirelon.aicalories.features.seller.categories.domain.AttributeInputType
import com.sirelon.aicalories.features.seller.categories.domain.OlxAttribute

@Composable
fun AttributeItem(attribute: OlxAttribute) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildString {
                append(attribute.label)
                if (attribute.unit.isNotBlank()) append(" (${attribute.unit})")
                if (attribute.validationRules.required) append(" *")
            },
        )
        Text(
            text = when (attribute.inputType) {
                AttributeInputType.SingleSelect -> "Single select — ${attribute.allowedValues.size} options"
                AttributeInputType.MultiSelect -> "Multi select — ${attribute.allowedValues.size} options"
                AttributeInputType.NumericInput -> buildString {
                    append("Numeric input")
                    attribute.validationRules.min?.let { append(", min: $it") }
                    attribute.validationRules.max?.let { append(", max: $it") }
                }

                AttributeInputType.TextInput -> "Text input"
            },
        )
        if (attribute.allowedValues.isNotEmpty()) {
            Text(text = attribute.allowedValues.joinToString { it.label })
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}
