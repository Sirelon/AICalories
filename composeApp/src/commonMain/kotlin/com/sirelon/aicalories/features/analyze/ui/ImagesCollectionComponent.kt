package com.sirelon.aicalories.features.analyze.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.sirelon.aicalories.designsystem.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagesCollectionComponent(
    files: Map<KmpFile, Double>,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val columnCount = remember(containerSize, density) {
        if (containerSize.width == 0 || containerSize.height == 0) {
            2
        } else {
            val widthDp = with(density) { containerSize.width.toDp() }
            val heightDp = with(density) { containerSize.height.toDp() }
            when {
                widthDp >= 600.dp -> 3
                widthDp > heightDp -> 3
                else -> 2
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = modifier.onSizeChanged { containerSize = it },
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = files.entries.toList(),
        ) { (file, progress) ->
            val fileName = file.getName(context).orEmpty()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = fileName,
                    style = AppTheme.typography.label,
                )
                AsyncImage(
                    model = file,
                    contentDescription = fileName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "$progress%",
                    style = AppTheme.typography.caption,
                )
            }
        }
    }
}
