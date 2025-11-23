@file:OptIn(ExperimentalMaterial3Api::class)

package com.sirelon.aicalories.features.agile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppLargeAppBar

@Composable
fun AgileScreen(
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val listState = rememberLazyListState()

    // FAB expands only near the top
    val fabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppLargeAppBar(
                title = "Agile",
                onBack = onBack,
                subtitle = "Calculation logic",
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                text = { Text("Add User story") },
                icon = {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                },
                expanded = fabExpanded,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            state = listState,
            contentPadding = it,
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(AppDimens.Spacing.xl3),
                    text = "Count of commands:",
                )
            }

            item {
                UserStoryCard()
            }
            item {
                UserStoryCard()
            }
            item {
                UserStoryCard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UserStoryCard() {
    var estimation by rememberSaveable { mutableStateOf(Estimation.M) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            text = "User Story #1",
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        Column(
            modifier = Modifier.padding(AppDimens.Spacing.xl3),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3),
        ) {
            EstimationChooser(
                selected = estimation,
                onSelected = { estimation = it },
            )

            Text("* Ticket #1")
            Text("* Ticket #2")
            Text("* Ticket #3")
            TextButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("Add ticket")
                },
            )
        }
    }
}

@Preview
@Composable
private fun AgileScreenPreview() {
    AgileScreen(onBack = {})
}
