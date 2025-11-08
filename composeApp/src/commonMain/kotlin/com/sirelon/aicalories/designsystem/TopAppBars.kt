package com.sirelon.aicalories.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppLargeAppBar(
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    LargeFlexibleTopAppBar(
        subtitle = if (subtitle != null) {
            {
                Text(text = subtitle)
            }
        } else null,
        title = {
            Text(text = title)
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}