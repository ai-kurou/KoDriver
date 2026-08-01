package kurou.kodriver.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
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
actual fun VersionMismatchBottomSheetEffect() {
    val viewModel: ServerConnectionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showVersionMismatchBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissVersionMismatchBottomSheet() },
            sheetState = sheetState,
        ) {
            VersionMismatchBottomSheetContent(
                windowsKoDriverVersion = uiState.serverVersion.orEmpty(),
                appVersion = uiState.appVersion,
                onDismiss = { viewModel.dismissVersionMismatchBottomSheet() },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VersionMismatchBottomSheetContentWindowsNewerPreview() {
    Surface {
        VersionMismatchBottomSheetContent(
            windowsKoDriverVersion = "2.0.0",
            appVersion = "1.0.0",
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VersionMismatchBottomSheetContentAppNewerPreview() {
    Surface {
        VersionMismatchBottomSheetContent(
            windowsKoDriverVersion = "1.0.0",
            appVersion = "2.0.0",
            onDismiss = {},
        )
    }
}
