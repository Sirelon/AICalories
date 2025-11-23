package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sirelon.aicalories.designsystem.AppDimens

@Composable
fun EstimationChooser(
    selected: Estimation,
    onSelected: (Estimation) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m),
    ) {
        Text(
            text = "Estimation",
            style = MaterialTheme.typography.titleMedium,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            onClick = { isSheetOpen = true },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.Spacing.xl3, vertical = AppDimens.Spacing.xl2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EstimationContent(estimation = selected)
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                )
            }
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            dragHandle = { SheetDefaults.DragHandle() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = AppDimens.Spacing.xl4,
                        horizontal = AppDimens.Spacing.xl4,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
            ) {
                Text(
                    text = "Select estimation",
                    style = MaterialTheme.typography.titleLarge,
                )
                Estimation.entries.forEach { estimation ->
                    EstimationOption(
                        estimation = estimation,
                        isSelected = estimation == selected,
                        onClick = {
                            onSelected(estimation)
                            isSheetOpen = false
                        },
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.Spacing.xl5))
            }
        }
    }
}

@Composable
private fun EstimationOption(
    estimation: Estimation,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppDimens.Spacing.xl2),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EstimationContent(estimation = estimation)
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = estimation.color(),
            )
        }
    }
}

@Composable
private fun EstimationContent(estimation: Estimation) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = estimation.icon(),
            contentDescription = null,
            tint = estimation.color(),
        )
        Text(
            text = estimation.code(),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.width(AppDimens.Spacing.xl))
        Text(
            text = estimation.description(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
