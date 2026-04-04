package com.sirelon.aicalories.features.attributes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sirelon.aicalories.features.attributes.domain.AttributeInputType
import com.sirelon.aicalories.features.attributes.domain.OlxAttribute
import com.sirelon.aicalories.features.attributes.presentation.AttributesContract
import com.sirelon.aicalories.features.attributes.presentation.AttributesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttributesScreen() {
    val viewModel: AttributesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    AttributesContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
private fun AttributesContent(
    state: AttributesContract.AttributesState,
    onEvent: (AttributesContract.AttributesEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            state.errorMessage != null -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = state.errorMessage)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onEvent(AttributesContract.AttributesEvent.Retry) }) {
                    Text("Retry")
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.attributes, key = { it.code }) { attribute ->
                    AttributeItem(attribute)
                }
            }
        }
    }
}

@Composable
private fun AttributeItem(attribute: OlxAttribute) {
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
