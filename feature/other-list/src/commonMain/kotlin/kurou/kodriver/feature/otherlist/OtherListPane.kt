package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Output
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.otherlist.generated.resources.Res
import kodriver.feature.otherlist.generated.resources.item_console_ip
import kodriver.feature.otherlist.generated.resources.item_exit_confirmation
import kodriver.feature.otherlist.generated.resources.item_github_repository
import kodriver.feature.otherlist.generated.resources.item_keep_screen_on
import kodriver.feature.otherlist.generated.resources.item_license
import kodriver.feature.otherlist.generated.resources.item_readout_start_sound
import kodriver.feature.otherlist.generated.resources.item_release_page
import kodriver.feature.otherlist.generated.resources.item_server_ip
import kodriver.feature.otherlist.generated.resources.item_volume
import kodriver.feature.otherlist.generated.resources.section_app_settings
import kodriver.feature.otherlist.generated.resources.section_connection_settings
import kodriver.feature.otherlist.generated.resources.section_information
import kodriver.feature.otherlist.generated.resources.section_readout_settings
import org.jetbrains.compose.resources.stringResource

private enum class OtherListSection {
    ConnectionSettings,
    ReadoutSettings,
    AppSettings,
    Information,
}

private val otherListSections = listOf(
    OtherListSection.ConnectionSettings,
    OtherListSection.ReadoutSettings,
    OtherListSection.AppSettings,
    OtherListSection.Information,
)

private fun OtherListItemType.section(): OtherListSection = when (this) {
    OtherListItemType.ServerIp,
    OtherListItemType.ConsoleIp,
    -> OtherListSection.ConnectionSettings
    OtherListItemType.Volume,
    OtherListItemType.ReadoutStartSound,
    -> OtherListSection.ReadoutSettings
    OtherListItemType.KeepScreenOn,
    OtherListItemType.ExitConfirmation,
    -> OtherListSection.AppSettings
    OtherListItemType.GitHubRepository,
    OtherListItemType.ReleasePage,
    OtherListItemType.License,
    -> OtherListSection.Information
}

@Composable
private fun otherItemDisplayName(itemType: OtherListItemType): String = when (itemType) {
    OtherListItemType.ServerIp -> stringResource(Res.string.item_server_ip)
    OtherListItemType.ConsoleIp -> stringResource(Res.string.item_console_ip)
    OtherListItemType.Volume -> stringResource(Res.string.item_volume)
    OtherListItemType.KeepScreenOn -> stringResource(Res.string.item_keep_screen_on)
    OtherListItemType.ReadoutStartSound -> stringResource(Res.string.item_readout_start_sound)
    OtherListItemType.ExitConfirmation -> stringResource(Res.string.item_exit_confirmation)
    OtherListItemType.GitHubRepository -> stringResource(Res.string.item_github_repository)
    OtherListItemType.ReleasePage -> stringResource(Res.string.item_release_page)
    OtherListItemType.License -> stringResource(Res.string.item_license)
}

@Composable
private fun otherListSectionTitle(section: OtherListSection): String = when (section) {
    OtherListSection.ConnectionSettings -> stringResource(Res.string.section_connection_settings)
    OtherListSection.ReadoutSettings -> stringResource(Res.string.section_readout_settings)
    OtherListSection.AppSettings -> stringResource(Res.string.section_app_settings)
    OtherListSection.Information -> stringResource(Res.string.section_information)
}

@Composable
private fun OtherListItemLeadingIcon(itemType: OtherListItemType, hasAppUpdate: Boolean) {
    when (itemType) {
        OtherListItemType.ServerIp -> Icon(imageVector = Icons.Outlined.Computer, contentDescription = null)
        OtherListItemType.ConsoleIp -> Icon(imageVector = Icons.Outlined.SportsEsports, contentDescription = null)
        OtherListItemType.Volume -> Icon(imageVector = Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null)
        OtherListItemType.KeepScreenOn -> Icon(imageVector = Icons.Outlined.BrightnessHigh, contentDescription = null)
        OtherListItemType.ReadoutStartSound -> Icon(imageVector = Icons.Outlined.MusicNote, contentDescription = null)
        OtherListItemType.ExitConfirmation -> Icon(imageVector = Icons.Outlined.Output, contentDescription = null)
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
        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        OtherListItemType.Volume,
        OtherListItemType.License,
        -> Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null)
        OtherListItemType.ReadoutStartSound,
        -> Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
        OtherListItemType.KeepScreenOn,
        OtherListItemType.ExitConfirmation,
        -> Unit
        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        -> Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
    }
}

@Composable
fun OtherListPane(
    uiState: OtherListUiState,
    onItemClick: (OtherListItemType) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onExitConfirmationEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        val groupedItems = uiState.items.groupBy { it.section() }
        otherListSections.forEach { section ->
            val sectionItems = groupedItems[section].orEmpty()
            if (sectionItems.isNotEmpty()) {
                item(key = "section_${section.name}") {
                    OtherListSectionHeader(section)
                    HorizontalDivider()
                }
                items(sectionItems, key = { it.id }) { item ->
                    OtherListItem(
                        item = item,
                        uiState = uiState,
                        onKeepScreenOnChange = onKeepScreenOnChange,
                        onExitConfirmationEnabledChange = onExitConfirmationEnabledChange,
                        onItemClick = onItemClick,
                    )
                    HorizontalDivider()
                }
            }
        }
        if (uiState.appVersionLabel.isNotBlank() && uiState.appVersion.isNotBlank()) {
            item(key = "app_version") {
                OtherAppVersionListItem(
                    appVersionLabel = uiState.appVersionLabel,
                    appVersion = uiState.appVersion,
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun OtherListSectionHeader(section: OtherListSection) {
    Text(
        text = otherListSectionTitle(section),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun OtherListItem(
    item: OtherListItemType,
    uiState: OtherListUiState,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onExitConfirmationEnabledChange: (Boolean) -> Unit,
    onItemClick: (OtherListItemType) -> Unit,
) {
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
            trailingContent = {
                when (item) {
                    OtherListItemType.KeepScreenOn -> Switch(
                        checked = uiState.keepScreenOn,
                        onCheckedChange = onKeepScreenOnChange,
                    )
                    OtherListItemType.ExitConfirmation -> Switch(
                        checked = uiState.exitConfirmationEnabled,
                        onCheckedChange = onExitConfirmationEnabledChange,
                    )
                    else -> OtherListItemTrailingIcon(item)
                }
            },
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
                .clickable {
                    when (item) {
                        OtherListItemType.KeepScreenOn -> onKeepScreenOnChange(!uiState.keepScreenOn)
                        OtherListItemType.ExitConfirmation ->
                            onExitConfirmationEnabledChange(!uiState.exitConfirmationEnabled)
                        else -> onItemClick(item)
                    }
                },
        )
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
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherListPanePreview() {
    OtherListPane(
        uiState = OtherListUiState(),
        onItemClick = {},
        onKeepScreenOnChange = {},
        onExitConfirmationEnabledChange = {},
    )
}
