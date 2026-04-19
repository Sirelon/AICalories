package com.sirelon.aicalories.designsystem

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.copy_pill_copied
import com.sirelon.aicalories.generated.resources.copy_pill_default
import com.sirelon.aicalories.generated.resources.error_pill_default
import com.sirelon.aicalories.generated.resources.ic_circle_alert
import com.sirelon.aicalories.generated.resources.ic_circle_check_big
import com.sirelon.aicalories.generated.resources.ic_copy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

const val CopyPillFeedbackDurationMs = 1400L

private val PillShape = CircleShape
private val PillHeight = AppDimens.Size.xl5
private val PillIconSize = AppDimens.Size.xl
private val PillHorizontalPadding = AppDimens.Spacing.m
private val PillVerticalPadding = AppDimens.Spacing.xs

@Composable
fun CopyPill(
    value: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    val containerColor = if (copied) {
        AppTheme.colors.success.copy(alpha = 0.18f)
    } else {
        AppTheme.colors.surfaceLow
    }
    val contentColor = if (copied) AppTheme.colors.success else AppTheme.colors.primary

    Surface(
        onClick = {
            scope.launch {
                clipboard.setText(AnnotatedString(value))
                copied = true
            }
        },
        modifier = modifier.height(PillHeight),
        shape = PillShape,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(if (copied) Res.drawable.ic_circle_check_big else Res.drawable.ic_copy),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(PillIconSize),
            )
            Spacer(Modifier.width(AppDimens.Spacing.xs))
            Text(
                text = stringResource(if (copied) Res.string.copy_pill_copied else Res.string.copy_pill_default),
                style = AppTheme.typography.caption,
                color = contentColor,
            )
        }
    }

    if (copied) {
        LaunchedEffect(Unit) {
            delay(CopyPillFeedbackDurationMs)
            copied = false
        }
    }
}

@Composable
fun ErrorPill(
    label: String = stringResource(Res.string.error_pill_default),
    modifier: Modifier = Modifier,
) {
    val errorColor = AppTheme.colors.error

    Surface(
        modifier = modifier.height(PillHeight),
        shape = PillShape,
        color = errorColor.copy(alpha = 0.18f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_circle_alert),
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(PillIconSize),
            )
            Spacer(Modifier.width(AppDimens.Spacing.xs))
            Text(
                text = label,
                style = AppTheme.typography.caption.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = errorColor,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CopyPillPreview() {
    AppTheme {
        CopyPill(value = "Sample text to copy")
    }
}

@PreviewLightDark
@Composable
private fun ErrorPillPreview() {
    AppTheme {
        ErrorPill()
    }
}
