package kurou.kodriver.feature.other

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.other.generated.resources.Res
import kodriver.feature.other.generated.resources.item_github_repository
import kodriver.feature.other.generated.resources.item_license
import org.jetbrains.compose.resources.stringResource

@Composable
private fun otherItemDisplayName(itemId: String): String = when (itemId) {
    OtherItemType.GitHubRepository.id -> stringResource(Res.string.item_github_repository)
    OtherItemType.License.id -> stringResource(Res.string.item_license)
    else -> itemId
}

@Composable
internal fun OtherListPane(
    uiState: OtherListUiState,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        itemsIndexed(uiState.items, key = { _, item -> item }) { index, item ->
            Surface(
                color = if (OtherItemType.fromId(item) == uiState.selectedItem) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ) {
                ListItem(
                    headlineContent = { Text(otherItemDisplayName(item)) },
                    leadingContent = when (OtherItemType.fromId(item)) {
                        OtherItemType.GitHubRepository -> {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Code,
                                    contentDescription = null,
                                )
                            }
                        }

                        OtherItemType.License -> {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                )
                            }
                        }

                        null -> null
                    },
                    colors = if (OtherItemType.fromId(item) == uiState.selectedItem) {
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    } else {
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            headlineColor = MaterialTheme.colorScheme.onSurface,
                            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item) }
                        .testTag("other_item_$index"),
                )
            }
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherListPanePreview() {
    OtherListPane(
        uiState = OtherListUiState(),
        onItemClick = {},
    )
}
