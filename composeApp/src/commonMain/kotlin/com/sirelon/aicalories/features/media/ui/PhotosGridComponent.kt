package com.sirelon.aicalories.features.media.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.io.KmpFile
import com.sirelon.aicalories.generated.resources.Res
import com.sirelon.aicalories.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.sirelon.aicalories.designsystem.AppAsyncImage
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.UploadStatusIndicator
import com.sirelon.aicalories.features.media.upload.UploadingItem

@Composable
fun PhotosGridComponent(
    files: Map<KmpFile, UploadingItem>,
    interactionEnabled: Boolean,
    onAddPhoto: () -> Unit,
    gridSize: Int = 3,
    maxFiles: Int = 5,
) {
    val emptyItems = maxFiles - files.size
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

                        if (upload.isUploading) {
                            UploadStatusIndicator(progress = upload.progress)
                        }
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
                            contentDescription = stringResource(Res.string.add_photo_cd),
                            modifier = Modifier.size(AppDimens.Size.xl8),
                        )
                    }
                )
            }
        }
    }
}

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
