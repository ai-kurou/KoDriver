package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import kodriver.feature.otherlist.generated.resources.Res
import kodriver.feature.otherlist.generated.resources.item_console_ip
import kodriver.feature.otherlist.generated.resources.item_github_repository
import kodriver.feature.otherlist.generated.resources.item_license
import kodriver.feature.otherlist.generated.resources.item_readout_start_sound
import kodriver.feature.otherlist.generated.resources.item_release_page
import kodriver.feature.otherlist.generated.resources.item_server_ip
import kodriver.feature.otherlist.generated.resources.item_volume
import org.jetbrains.compose.resources.stringResource

@Composable
private fun otherItemDisplayName(itemType: OtherListItemType): String = when (itemType) {
    OtherListItemType.ServerIp -> stringResource(Res.string.item_server_ip)
    OtherListItemType.ConsoleIp -> stringResource(Res.string.item_console_ip)
    OtherListItemType.Volume -> stringResource(Res.string.item_volume)
    OtherListItemType.ReadoutStartSound -> stringResource(Res.string.item_readout_start_sound)
    OtherListItemType.GitHubRepository -> stringResource(Res.string.item_github_repository)
    OtherListItemType.ReleasePage -> stringResource(Res.string.item_release_page)
    OtherListItemType.License -> stringResource(Res.string.item_license)
}

@Composable
private fun OtherListItemLeadingIcon(itemType: OtherListItemType, hasAppUpdate: Boolean) {
    when (itemType) {
        OtherListItemType.ServerIp -> Icon(imageVector = Icons.Outlined.Wifi, contentDescription = null)
        OtherListItemType.ConsoleIp -> Icon(imageVector = Icons.Outlined.SportsEsports, contentDescription = null)
        OtherListItemType.Volume -> Icon(imageVector = Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null)
        OtherListItemType.ReadoutStartSound -> Icon(imageVector = Icons.Outlined.MusicNote, contentDescription = null)
        OtherListItemType.GitHubRepository -> Icon(imageVector = Icons.Outlined.Code, contentDescription = null)
        OtherListItemType.ReleasePage -> BadgedBox(badge = { if (hasAppUpdate) Badge() }) {
            Icon(imageVector = Icons.Outlined.NewReleases, contentDescription = null)
        }
        OtherListItemType.License -> Icon(imageVector = Icons.Outlined.Description, contentDescription = null)
    }
}

@Composable
private fun OtherListItemTrailingIcon(itemType: OtherListItemType) {
    when (itemType) {
        OtherListItemType.ServerIp -> Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
        OtherListItemType.ConsoleIp -> Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
        OtherListItemType.ReadoutStartSound -> Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
        OtherListItemType.Volume,
        OtherListItemType.License,
        -> Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null)
        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        -> Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
    }
}

@Composable
fun OtherListPane(
    uiState: OtherListUiState,
    onItemClick: (OtherListItemType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        itemsIndexed(uiState.items, key = { _, item -> item.id }) { index, item ->
            Surface(
                color = if (item == uiState.selectedItem) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ) {
                ListItem(
                    headlineContent = { Text(otherItemDisplayName(item)) },
                    leadingContent = { OtherListItemLeadingIcon(item, uiState.hasAppUpdate) },
                    trailingContent = { OtherListItemTrailingIcon(item) },
                    colors = if (item == uiState.selectedItem) {
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    } else {
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            headlineColor = MaterialTheme.colorScheme.onSurface,
                            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
        item(key = "app_version") {
            OtherAppVersionListItem(
                appVersionLabel = uiState.appVersionLabel,
                appVersion = uiState.appVersion,
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun OtherAppVersionListItem(
    appVersionLabel: String,
    appVersion: String,
) {
    if (appVersionLabel.isBlank() || appVersion.isBlank()) return

    ListItem(
        headlineContent = { Text(appVersionLabel) },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
            )
        },
        trailingContent = {
            Text(
                text = appVersion,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("other_app_version"),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherListPanePreview() {
    OtherListPane(
        uiState = OtherListUiState(),
        onItemClick = {},
    )
}
