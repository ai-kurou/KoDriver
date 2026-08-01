package kurou.kodriver.feature.otherlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.otherlist.generated.resources.Res
import kodriver.feature.otherlist.generated.resources.item_console_ip
import kodriver.feature.otherlist.generated.resources.item_debug_state
import kodriver.feature.otherlist.generated.resources.item_dynamic_color
import kodriver.feature.otherlist.generated.resources.item_exit_confirmation
import kodriver.feature.otherlist.generated.resources.item_github_repository
import kodriver.feature.otherlist.generated.resources.item_keep_screen_on
import kodriver.feature.otherlist.generated.resources.item_license
import kodriver.feature.otherlist.generated.resources.item_readout_start_sound
import kodriver.feature.otherlist.generated.resources.item_release_page
import kodriver.feature.otherlist.generated.resources.item_server_ip
import kodriver.feature.otherlist.generated.resources.item_theme
import kodriver.feature.otherlist.generated.resources.item_volume
import kodriver.feature.otherlist.generated.resources.section_app_settings
import kodriver.feature.otherlist.generated.resources.section_connection_settings
import kodriver.feature.otherlist.generated.resources.section_information
import kodriver.feature.otherlist.generated.resources.section_readout_settings
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private enum class OtherListSection {
    ConnectionSettings,
    ReadoutSettings,
    AppSettings,
    Information,
}

private val otherListSections =
    listOf(
    OtherListSection.ConnectionSettings,
    OtherListSection.ReadoutSettings,
    OtherListSection.AppSettings,
    OtherListSection.Information,
)

private fun OtherListItemType.section(): OtherListSection =
    when (this) {
    OtherListItemType.ServerIp,
    OtherListItemType.ConsoleIp,
    -> OtherListSection.ConnectionSettings

    OtherListItemType.Volume,
    OtherListItemType.ReadoutStartSound,
    -> OtherListSection.ReadoutSettings

    OtherListItemType.KeepScreenOn,
    OtherListItemType.ExitConfirmation,
    OtherListItemType.Theme,
    OtherListItemType.DynamicColor,
    -> OtherListSection.AppSettings

    OtherListItemType.GitHubRepository,
    OtherListItemType.ReleasePage,
    OtherListItemType.License,
    -> OtherListSection.Information

    OtherListItemType.DebugState -> OtherListSection.Information
}

@Composable
private fun otherItemDisplayName(itemType: OtherListItemType): String =
    when (itemType) {
    OtherListItemType.ServerIp -> stringResource(Res.string.item_server_ip)
    OtherListItemType.ConsoleIp -> stringResource(Res.string.item_console_ip)
    OtherListItemType.Volume -> stringResource(Res.string.item_volume)
    OtherListItemType.KeepScreenOn -> stringResource(Res.string.item_keep_screen_on)
    OtherListItemType.ReadoutStartSound -> stringResource(Res.string.item_readout_start_sound)
    OtherListItemType.ExitConfirmation -> stringResource(Res.string.item_exit_confirmation)
    OtherListItemType.Theme -> stringResource(Res.string.item_theme)
    OtherListItemType.DynamicColor -> stringResource(Res.string.item_dynamic_color)
    OtherListItemType.GitHubRepository -> stringResource(Res.string.item_github_repository)
    OtherListItemType.ReleasePage -> stringResource(Res.string.item_release_page)
    OtherListItemType.License -> stringResource(Res.string.item_license)
    OtherListItemType.DebugState -> stringResource(Res.string.item_debug_state)
}

@Composable
private fun otherListSectionTitle(section: OtherListSection): String =
    when (section) {
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

        OtherListItemType.Theme -> Icon(imageVector = Icons.Outlined.BrightnessHigh, contentDescription = null)

        OtherListItemType.DynamicColor -> Icon(imageVector = Icons.Outlined.Palette, contentDescription = null)

        OtherListItemType.GitHubRepository -> Icon(imageVector = Icons.Outlined.Code, contentDescription = null)

        OtherListItemType.ReleasePage ->
            BadgedBox(badge = { if (hasAppUpdate) Badge() }) {
            Icon(imageVector = Icons.Outlined.NewReleases, contentDescription = null)
        }

        OtherListItemType.License -> Icon(imageVector = Icons.Outlined.Description, contentDescription = null)

        OtherListItemType.DebugState -> Icon(imageVector = Icons.Outlined.Code, contentDescription = null)
    }
}

@Composable
private fun OtherListItemTrailingIcon(itemType: OtherListItemType) {
    when (itemType) {
        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        OtherListItemType.Volume,
        OtherListItemType.License,
        OtherListItemType.DebugState,
        -> Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null)

        OtherListItemType.ReadoutStartSound,
        OtherListItemType.Theme,
        -> Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)

        OtherListItemType.KeepScreenOn,
        OtherListItemType.ExitConfirmation,
        OtherListItemType.DynamicColor,
        -> Unit

        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        -> Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
    }
}

/**
 * OtherList の画面を表示する Composable。
 */
@Composable
fun OtherListPane(
    uiState: OtherListUiState,
    onItemClick: (OtherListItemType) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onExitConfirmationEnabledChange: (Boolean) -> Unit,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onAppVersionTapped: () -> Unit = {},
    modifier: Modifier = Modifier,
    scrollToTopRequest: Int = 0,
) {
    val listState = rememberLazyListState()

    ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier =
            modifier
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
                        onDynamicColorEnabledChange = onDynamicColorEnabledChange,
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
                    onAppVersionTapped = onAppVersionTapped,
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ScrollToTopEffect(
    scrollToTopRequest: Int,
    scrollToTop: suspend () -> Unit,
) {
    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0) {
            scrollToTop()
        }
    }
}

@Composable
private fun OtherListSectionHeader(section: OtherListSection) {
    Text(
        text = otherListSectionTitle(section),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
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
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onItemClick: (OtherListItemType) -> Unit,
) {
    val isSelected = item == uiState.selectedItem
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
            animationSpec = tween(durationMillis = 500),
        label = "otherListItemContainerColor",
    )
    val headlineColor by animateColorAsState(
        targetValue =
            if (isSelected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
            animationSpec = tween(durationMillis = 500),
        label = "otherListItemHeadlineColor",
    )
    val iconColor by animateColorAsState(
        targetValue =
            if (isSelected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
            animationSpec = tween(durationMillis = 500),
        label = "otherListItemIconColor",
    )

    ListItem(
        headlineContent = { Text(otherItemDisplayName(item)) },
        leadingContent = { OtherListItemLeadingIcon(item, uiState.hasAppUpdate) },
        trailingContent = {
            when (item) {
                OtherListItemType.KeepScreenOn ->
                    Switch(
                    checked = uiState.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                )

                OtherListItemType.ExitConfirmation ->
                    Switch(
                    checked = uiState.exitConfirmationEnabled,
                    onCheckedChange = onExitConfirmationEnabledChange,
                )

                OtherListItemType.DynamicColor ->
                    Switch(
                    checked = uiState.dynamicColorEnabled,
                    onCheckedChange = onDynamicColorEnabledChange,
                )

                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Theme,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.License,
                OtherListItemType.DebugState,
                -> OtherListItemTrailingIcon(item)
            }
        },
        colors =
            ListItemDefaults.colors(
            containerColor = containerColor,
            headlineColor = headlineColor,
            leadingIconColor = iconColor,
            trailingIconColor = iconColor,
        ),
            modifier =
                Modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .clickable {
                when (item) {
                    OtherListItemType.KeepScreenOn -> {
                        onKeepScreenOnChange(!uiState.keepScreenOn)
                    }

                    OtherListItemType.ExitConfirmation -> {
                        onExitConfirmationEnabledChange(!uiState.exitConfirmationEnabled)
                    }

                    OtherListItemType.DynamicColor -> {
                        onDynamicColorEnabledChange(!uiState.dynamicColorEnabled)
                    }

                    OtherListItemType.ServerIp,
                    OtherListItemType.ConsoleIp,
                    OtherListItemType.Volume,
                    OtherListItemType.ReadoutStartSound,
                    OtherListItemType.Theme,
                    OtherListItemType.GitHubRepository,
                    OtherListItemType.ReleasePage,
                    OtherListItemType.License,
                    OtherListItemType.DebugState,
                    -> {
                        onItemClick(item)
                    }
                }
            },
                )
}

private const val DEBUG_STATE_TAP_THRESHOLD = 5
private val DEBUG_STATE_TAP_TIMEOUT = 1.seconds

@Composable
private fun OtherAppVersionListItem(
    appVersionLabel: String,
    appVersion: String,
    onAppVersionTapped: () -> Unit,
) {
    if (appVersionLabel.isBlank() || appVersion.isBlank()) return

    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapMark by remember { mutableStateOf<TimeMark?>(null) }

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
        colors =
            ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
            modifier =
                Modifier
            .fillMaxWidth()
            .clickable {
                val now = TimeSource.Monotonic.markNow()
                val elapsedSinceLastTap = lastTapMark?.elapsedNow()
                tapCount =
                    if (elapsedSinceLastTap != null && elapsedSinceLastTap < DEBUG_STATE_TAP_TIMEOUT) {
                    tapCount + 1
                } else {
                    1
                }
                lastTapMark = now
                if (tapCount >= DEBUG_STATE_TAP_THRESHOLD) {
                    tapCount = 0
                    onAppVersionTapped()
                }
            },
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
        onDynamicColorEnabledChange = {},
    )
}
