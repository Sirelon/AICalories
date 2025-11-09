package com.sirelon.aicalories.features.analyze.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.designsystem.AppAsyncImage
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.UploadStatusIndicator
import com.sirelon.aicalories.features.analyze.presentation.UploadItem

@Composable
fun PhotosGridComponent(
    files: Map<KmpFile, UploadItem>,
    canAddMore: Boolean,
    interactionEnabled: Boolean,
    onAddPhoto: () -> Unit,
    gridSize: Int = 3,
) {
    val emptyItems = gridSize - files.size + if (canAddMore) 1 else 0
    val spacing = AppDimens.Spacing.xl3
    val arrangement = Arrangement.spacedBy(spacing)
    BoxWithConstraints {
        val itemWidth = maxWidth / gridSize - spacing
        val itemModifier = Modifier
            .size(itemWidth)
            .aspectRatio(1f)

        FlowRow(
            horizontalArrangement = arrangement,
            verticalArrangement = arrangement,
        ) {
            files.forEach { (file, upload) ->
                PhotoContainer(
                    modifier = itemModifier,
                    onClick = {
                        // TODO: remove photo
                    },
                    enabled = interactionEnabled,
                    content = {
                        AppAsyncImage(
                            modifier = Modifier,
                            model = file,
                        )

                        UploadStatusIndicator(progress = upload.progress)
                    }
                )
            }

            repeat(emptyItems) {
                PhotoContainer(
                    modifier = itemModifier,
                    onClick = onAddPhoto,
                    enabled = interactionEnabled,
                    content = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            tint = AppTheme.colors.onSurface.copy(alpha = 0.6f),
                            contentDescription = "Add photo",
                            modifier = Modifier.size(AppDimens.Size.xl8),
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoContainer(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl3),
        border = BorderStroke(
            width = AppDimens.BorderWidth.l,
            color = AppTheme.colors.outline.copy(alpha = if (enabled) 1f else 0.4f),
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
