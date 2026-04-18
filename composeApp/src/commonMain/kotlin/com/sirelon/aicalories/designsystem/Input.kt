package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.character_count_range
import com.sirelon.aicalories.generated.resources.copy
import com.sirelon.aicalories.generated.resources.ic_copy
import com.sirelon.aicalories.generated.resources.min_characters
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    minCharacters: Int = -1,
    maxCharacters: Int = -1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val characterCountText = when {
        minCharacters > 0 && value.length < minCharacters -> stringResource(Res.string.min_characters, minCharacters)
        maxCharacters > 0 -> stringResource(Res.string.character_count_range, value.length, maxCharacters)
        else -> supportingText
    }

    // Filled variant per design spec: surface-low background, no border,
    // 2px primary indicator on focus only
    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.surfaceLow,
            unfocusedContainerColor = AppTheme.colors.surfaceLow,
            disabledContainerColor = AppTheme.colors.surfaceLow.copy(alpha = 0.6f),
            errorContainerColor = AppTheme.colors.surfaceLow,
            focusedIndicatorColor = AppTheme.colors.primary,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = AppTheme.colors.error,
            focusedTextColor = AppTheme.colors.onSurface,
            unfocusedTextColor = AppTheme.colors.onSurface,
            focusedLabelColor = AppTheme.colors.primary,
            unfocusedLabelColor = AppTheme.colors.onSurface.copy(alpha = 0.6f),
            cursorColor = AppTheme.colors.primary,
        ),
        label = label?.let {
            {
                Text(
                    text = it,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            }
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.5f),
                )
            }
        },
        supportingText = characterCountText?.let {
            {
                Text(
                    text = it,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        suffix = suffix,
        prefix = prefix,
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        textStyle = AppTheme.typography.body,
    )
}

@Composable
fun InputWithCopy(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    minCharacters: Int = -1,
    maxCharacters: Int = Int.MIN_VALUE,
    prefix: @Composable (() -> Unit)? = null
) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val characterCountText = when {
        minCharacters > 0 && state.text.length < minCharacters -> stringResource(Res.string.min_characters, minCharacters)
        maxCharacters > 0 -> stringResource(Res.string.character_count_range, state.text.length, maxCharacters)
        else -> null
    }

    TextField(
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.surfaceLow,
            unfocusedContainerColor = AppTheme.colors.surfaceLow,
            focusedIndicatorColor = AppTheme.colors.primary,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        state = state,
        modifier = modifier.fillMaxWidth(),
        prefix = prefix,
        keyboardOptions = keyboardOptions,
        lineLimits = lineLimits,
        supportingText = characterCountText?.let {
            {
                Text(
                    text = it,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.outline,
                )
            }
        },
        trailingIcon = {
            TextButton(
                onClick = {
                    scope.launch {
                        clipboard.setText(AnnotatedString(state.text.toString()))
                    }
                },
            ) {
                Icon(
                    modifier = Modifier.size(AppDimens.Size.xl3),
                    painter = painterResource(Res.drawable.ic_copy),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(AppDimens.Spacing.l))
                Text(stringResource(Res.string.copy), style = AppTheme.typography.label)
            }
        },
    )
}
