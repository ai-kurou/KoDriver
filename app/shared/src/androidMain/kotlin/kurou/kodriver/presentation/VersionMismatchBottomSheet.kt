package kurou.kodriver.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.feature.serverconnection.ServerConnectionViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * VersionMismatchBottomSheetEffect のこのプラットフォーム向け実装。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun VersionMismatchBottomSheetEffect(hapticFeedbackEnabled: Boolean) {
    val viewModel: ServerConnectionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showVersionMismatchBottomSheet) {
        val sheetState =
            rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            )
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissVersionMismatchBottomSheet() },
            sheetState = sheetState,
        ) {
            VersionMismatchBottomSheetContent(
                windowsKoDriverVersion = uiState.serverVersion.orEmpty(),
                appVersion = uiState.appVersion,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                onDismiss = { viewModel.dismissVersionMismatchBottomSheet() },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VersionMismatchBottomSheetContentWindowsNewerPreview() {
    AppTheme {
        Surface {
            VersionMismatchBottomSheetContent(
                windowsKoDriverVersion = "2.0.0",
                appVersion = "1.0.0",
                hapticFeedbackEnabled = true,
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VersionMismatchBottomSheetContentAppNewerPreview() {
    AppTheme {
        Surface {
            VersionMismatchBottomSheetContent(
                windowsKoDriverVersion = "1.0.0",
                appVersion = "2.0.0",
                hapticFeedbackEnabled = true,
                onDismiss = {},
            )
        }
    }
}
