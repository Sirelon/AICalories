package com.sirelon.aicalories.features.datagenerator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberRangeSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleValueSelector(
    label: String,
    value: Double,
    bounds: ClosedFloatingPointRange<Double>,
    step: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeStep = if (step > 0.0) step else 1.0
    val decimals = remember(safeStep) { decimalsForStep(safeStep) }
    val clampedValue = value.coerceIn(bounds.start, bounds.endInclusive)
    val displayValue = formatValue(clampedValue, decimals)
    val sliderSteps = remember(bounds, safeStep) { calculateSliderSteps(bounds, safeStep) }
    val canDecrease = clampedValue > bounds.start + 1e-6
    val canIncrease = clampedValue < bounds.endInclusive - 1e-6

    fun updateValue(nextValue: Double) {
        val snapped = snapToStep(nextValue, bounds, safeStep)
        if (!nearlyEqual(snapped, clampedValue)) {
            onValueChange(snapped)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
    ) {
        ValueHeaderRow(
            label = label,
            valueText = displayValue
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
        ) {
            StepIconButton(
                enabled = canDecrease,
                onClick = { updateValue(clampedValue - safeStep) },
                icon = Icons.Default.Remove,
                contentDescription = "Decrease value"
            )

            Slider(
                value = clampedValue.toFloat(),
                onValueChange = { updateValue(it.toDouble()) },
                valueRange = bounds.start.toFloat()..bounds.endInclusive.toFloat(),
                steps = sliderSteps,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    activeTrackColor = AppTheme.colors.primary,
                    activeTickColor = AppTheme.colors.primary.copy(alpha = 0.35f),
                    inactiveTrackColor = AppTheme.colors.outline.copy(alpha = 0.25f),
                    inactiveTickColor = AppTheme.colors.outline.copy(alpha = 0.2f),
                    thumbColor = AppTheme.colors.primary
                )
            )

            StepIconButton(
                enabled = canIncrease,
                onClick = { updateValue(clampedValue + safeStep) },
                icon = Icons.Default.Add,
                contentDescription = "Increase value"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeSelector(
    label: String,
    min: Double?,
    max: Double?,
    bounds: ClosedFloatingPointRange<Double>,
    step: Double,
    onRangeChange: (Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
    allowNull: Boolean = true,
    anyLabel: String = "Any",
) {
    val safeStep = if (step > 0.0) step else 1.0
    val decimals = remember(safeStep) { decimalsForStep(safeStep) }
    var hintText by remember { mutableStateOf<String?>(null) }
    var suppressSliderCallback by remember { mutableStateOf(false) }

    LaunchedEffect(hintText) {
        if (hintText != null) {
            delay(2200)
            hintText = null
        }
    }

    val displayRangeText = formatRangeLabel(min, max, decimals, anyLabel)
    val sliderMin = (min ?: bounds.start).coerceIn(bounds.start, bounds.endInclusive)
    val sliderMax = (max ?: bounds.endInclusive).coerceIn(bounds.start, bounds.endInclusive)
    val sliderValue = if (sliderMin <= sliderMax) sliderMin..sliderMax else sliderMax..sliderMin
    val sliderSteps = remember(bounds, safeStep) { calculateSliderSteps(bounds, safeStep) }
    val sliderState = rememberRangeSliderState(
        activeRangeStart = sliderValue.start.toFloat(),
        activeRangeEnd = sliderValue.endInclusive.toFloat(),
        steps = sliderSteps,
        valueRange = bounds.start.toFloat()..bounds.endInclusive.toFloat(),
        onValueChangeFinished = null
    )

    fun applyRangeChange(targetMin: Double?, targetMax: Double?, showStepHint: Boolean = true) {
        val fixed = normalizeRange(targetMin, targetMax, bounds, safeStep, allowNull, showStepHint)
        if (fixed.hint != null) hintText = fixed.hint
        onRangeChange(fixed.min, fixed.max)
    }

    LaunchedEffect(sliderValue.start, sliderValue.endInclusive) {
        suppressSliderCallback = true
        sliderState.activeRangeStart = sliderValue.start.toFloat()
        sliderState.activeRangeEnd = sliderValue.endInclusive.toFloat()
        suppressSliderCallback = false
    }

    LaunchedEffect(sliderState) {
        snapshotFlow { sliderState.activeRangeStart to sliderState.activeRangeEnd }
            .distinctUntilChanged()
            .collect { (start, end) ->
                if (!suppressSliderCallback) {
                    applyRangeChange(start.toDouble(), end.toDouble(), showStepHint = false)
                }
            }
    }

    val minCanDecrease = min == null || min > bounds.start + 1e-6
    val maxCanIncrease = max == null || max < bounds.endInclusive - 1e-6

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
    ) {
        ValueHeaderRow(
            label = label,
            valueText = displayRangeText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
        ) {
            StepIconButton(
                enabled = minCanDecrease,
                onClick = {
                    val nextMin = (min ?: bounds.start) - safeStep
                    applyRangeChange(nextMin, max)
                },
                icon = Icons.Default.Remove,
                contentDescription = "Decrease minimum"
            )

            RangeSlider(
                state = sliderState,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    activeTrackColor = AppTheme.colors.primary,
                    activeTickColor = AppTheme.colors.primary.copy(alpha = 0.35f),
                    inactiveTrackColor = AppTheme.colors.outline.copy(alpha = 0.25f),
                    inactiveTickColor = AppTheme.colors.outline.copy(alpha = 0.2f),
                    thumbColor = AppTheme.colors.primary
                )
            )

            StepIconButton(
                enabled = maxCanIncrease,
                onClick = {
                    val nextMax = (max ?: bounds.endInclusive) + safeStep
                    applyRangeChange(min, nextMax)
                },
                icon = Icons.Default.Add,
                contentDescription = "Increase maximum"
            )
        }

        AnimatedVisibility(visible = hintText != null) {
            Text(
                text = hintText.orEmpty(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StepIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun ValueHeaderRow(
    label: String,
    valueText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppTheme.typography.body,
            color = AppTheme.colors.onSurface
        )
        Text(
            text = valueText,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.onSurface.copy(alpha = 0.65f)
        )
    }
}

private data class RangeFixResult(
    val min: Double?,
    val max: Double?,
    val hint: String?
)

private fun normalizeRange(
    min: Double?,
    max: Double?,
    bounds: ClosedFloatingPointRange<Double>,
    step: Double,
    allowNull: Boolean,
    showStepHint: Boolean,
): RangeFixResult {
    var hint: String? = null
    val minOutOfBounds = min != null && (min < bounds.start || min > bounds.endInclusive)
    val maxOutOfBounds = max != null && (max < bounds.start || max > bounds.endInclusive)
    var fixedMin = min?.let { snapToStep(it, bounds, step) }
    var fixedMax = max?.let { snapToStep(it, bounds, step) }
    val minAdjusted = min != null && fixedMin != null && !nearlyEqual(min, fixedMin)
    val maxAdjusted = max != null && fixedMax != null && !nearlyEqual(max, fixedMax)

    if (!allowNull) {
        if (fixedMin == null) fixedMin = bounds.start
        if (fixedMax == null) fixedMax = bounds.endInclusive
    }

    if (fixedMin != null && fixedMax != null && fixedMin > fixedMax) {
        val swap = fixedMin
        fixedMin = fixedMax
        fixedMax = swap
        hint = "Adjusted to keep min <= max."
    } else if (showStepHint && (minOutOfBounds || maxOutOfBounds) && (minAdjusted || maxAdjusted)) {
        hint = "Adjusted to fit bounds."
    }

    return RangeFixResult(min = fixedMin, max = fixedMax, hint = hint)
}

private fun snapToStep(
    value: Double,
    bounds: ClosedFloatingPointRange<Double>,
    step: Double
): Double {
    val clamped = value.coerceIn(bounds.start, bounds.endInclusive)
    val steps = ((clamped - bounds.start) / step).roundToInt()
    val snapped = bounds.start + steps * step
    return snapped.coerceIn(bounds.start, bounds.endInclusive)
}

private fun decimalsForStep(step: Double): Int {
    var decimals = 0
    var scaled = step
    while (decimals < 6 && abs(scaled - round(scaled)) > 1e-6) {
        scaled *= 10
        decimals++
    }
    return decimals
}

private fun formatRangeLabel(
    min: Double?,
    max: Double?,
    decimals: Int,
    anyLabel: String
): String {
    if (min == null && max == null) return anyLabel
    val minText = min?.let { formatValue(it, decimals) } ?: anyLabel
    val maxText = max?.let { formatValue(it, decimals) } ?: anyLabel
    return "$minText - $maxText"
}

private fun formatValue(value: Double, decimals: Int): String {
    if (decimals <= 0) {
        return round(value).toLong().toString()
    }
    val factor = 10.0.pow(decimals.toDouble())
    val scaled = round(abs(value) * factor).toLong()
    val whole = scaled / factor.toLong()
    val fraction = (scaled % factor.toLong()).toString().padStart(decimals, '0')
    val sign = if (value < 0) "-" else ""
    return "$sign$whole.$fraction"
}

private fun nearlyEqual(a: Double, b: Double): Boolean {
    return abs(a - b) < 1e-6
}

private fun calculateSliderSteps(
    bounds: ClosedFloatingPointRange<Double>,
    step: Double
): Int {
    val range = bounds.endInclusive - bounds.start
    if (range <= 0.0 || step <= 0.0) return 0
    val raw = range / step
    val rounded = raw.roundToInt()
    return if (abs(raw - rounded) < 1e-4 && rounded >= 1) {
        rounded - 1
    } else {
        0
    }
}

@Preview
@Composable
private fun RangeSelectorIntPreview() {
    var min by remember { mutableStateOf(3.0) }
    var max by remember { mutableStateOf(8.0) }

    Column(modifier = Modifier.padding(16.dp)) {
        RangeSelector(
            label = "People per team",
            min = min,
            max = max,
            bounds = 1.0..20.0,
            step = 1.0,
            allowNull = false,
            onRangeChange = { newMin, newMax ->
                if (newMin != null && newMax != null) {
                    min = newMin
                    max = newMax
                }
            }
        )
    }
}

@Preview
@Composable
private fun RangeSelectorDoublePreview() {
    var min by remember { mutableStateOf(0.1) }
    var max by remember { mutableStateOf(0.45) }

    Column(modifier = Modifier.padding(16.dp)) {
        RangeSelector(
            label = "Risk factor",
            min = min,
            max = max,
            bounds = 0.0..1.0,
            step = 0.05,
            allowNull = true,
            onRangeChange = { newMin, newMax ->
                min = newMin
                max = newMax
            }
        )
    }
}
