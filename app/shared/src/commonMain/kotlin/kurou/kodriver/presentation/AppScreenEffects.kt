package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
    LaunchedEffect(Unit) {
        checkUpdate()
    }

    LaunchedEffect(darkTheme) {
        onDarkThemeChanged(darkTheme)
    }
}

@Composable
internal fun AppExitRequestEffect(
    exitRequested: Boolean,
    exitConfirmationEnabled: Boolean,
    onExitRequestConsumed: () -> Unit,
    onShowExitConfirmationDialog: () -> Unit,
    onExit: () -> Unit,
) {
    LaunchedEffect(exitRequested) {
        if (exitRequested) {
            onExitRequestConsumed()
            if (exitConfirmationEnabled) {
                onShowExitConfirmationDialog()
            } else {
                onExit()
            }
        }
    }
}

@Composable
internal fun AppNarratorEffects() {
    LmuWindowsNarratorEffect()
    Gt7Ps5NarratorEffect()
    AceWindowsNarratorEffect()
    VersionMismatchBottomSheetEffect()
}

@Composable
internal fun ExitConfirmationDialogHost(
    visible: Boolean,
    darkTheme: Boolean,
    dynamicColorEnabled: Boolean,
    coroutineScope: CoroutineScope,
    saveExitConfirmationEnabled: suspend (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onExit: () -> Unit,
) {
    if (!visible) return

    AppTheme(darkTheme = darkTheme, dynamicColor = dynamicColorEnabled) {
        ExitConfirmationDialog(
            onDismiss = onDismiss,
            onConfirm = { doNotShowAgain ->
                coroutineScope.launch {
                    saveExitConfirmationPreferenceForExit(
                        doNotShowAgain = doNotShowAgain,
                        saveExitConfirmationEnabled = saveExitConfirmationEnabled,
                    )
                    onDismiss()
                    onExit()
                }
            },
        )
    }
}
