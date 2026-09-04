package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kurou.kodriver.feature.acewindowsnarrator.AceWindowsNarratorEffect
import kurou.kodriver.feature.gt7ps5narrator.Gt7Ps5NarratorEffect
import kurou.kodriver.feature.lmuwindowsnarrator.LmuWindowsNarratorEffect
import kurou.kodriver.feature.otherlist.OtherListItemType

internal fun <T> handleTabReselected(
    selectedItem: T?,
    clearSelectedItem: () -> Unit,
    requestScrollToTop: () -> Unit,
) {
    if (selectedItem != null) {
        clearSelectedItem()
    } else {
        requestScrollToTop()
    }
}

@Composable
internal fun rememberConnectionBannerTap(
    bannerUiState: ConnectionBannerUiState,
    onSelectOtherItem: (OtherListItemType) -> Unit,
): (() -> Unit)? =
    remember(
        bannerUiState.isTappable,
        bannerUiState.tapNavigationTarget,
        onSelectOtherItem,
    ) {
        if (bannerUiState.isTappable && bannerUiState.tapNavigationTarget != null) {
            {
                onSelectOtherItem(bannerUiState.tapNavigationTarget.toOtherListItemType())
            }
        } else {
            null
        }
    }

@Composable
internal fun AppStartupEffects(
    darkTheme: Boolean,
    checkUpdate: suspend () -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
) {
    val currentCheckUpdate by rememberUpdatedState(checkUpdate)
    val currentOnDarkThemeChange by rememberUpdatedState(onDarkThemeChanged)

    LaunchedEffect(Unit) {
        currentCheckUpdate()
    }

    LaunchedEffect(darkTheme) {
        currentOnDarkThemeChange(darkTheme)
    }
}

@Composable
internal fun AppNarratorEffects() {
    LmuWindowsNarratorEffect()
    Gt7Ps5NarratorEffect()
    AceWindowsNarratorEffect()
    VersionMismatchBottomSheetEffect()
}
