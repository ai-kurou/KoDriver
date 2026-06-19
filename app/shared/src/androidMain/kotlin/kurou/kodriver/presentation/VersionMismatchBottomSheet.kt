package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.version_mismatch_app_version
import kodriver.app.shared.generated.resources.version_mismatch_body
import kodriver.app.shared.generated.resources.version_mismatch_close
import kodriver.app.shared.generated.resources.version_mismatch_title
import kodriver.app.shared.generated.resources.version_mismatch_update_app
import kodriver.app.shared.generated.resources.version_mismatch_update_windows
import kodriver.app.shared.generated.resources.version_mismatch_windows_version
import kurou.kodriver.feature.serverconnection.ServerConnectionViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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

@Composable
private fun VersionMismatchBottomSheetContent(
    windowsKoDriverVersion: String,
    appVersion: String,
    onDismiss: () -> Unit,
) {
    val windowsParsed = parseVersion(windowsKoDriverVersion)
    val appParsed = parseVersion(appVersion)
    val windowsIsNewer = windowsParsed > appParsed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = stringResource(Res.string.version_mismatch_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.version_mismatch_body),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.version_mismatch_windows_version, windowsKoDriverVersion),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(Res.string.version_mismatch_app_version, appVersion),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (windowsIsNewer) {
                stringResource(Res.string.version_mismatch_update_app)
            } else {
                stringResource(Res.string.version_mismatch_update_windows)
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.version_mismatch_close))
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

private fun parseVersion(version: String): List<Int> =
    version.split(".").map { it.toIntOrNull() ?: 0 }

private operator fun List<Int>.compareTo(other: List<Int>): Int {
    val size = maxOf(this.size, other.size)
    for (i in 0 until size) {
        val a = this.getOrElse(i) { 0 }
        val b = other.getOrElse(i) { 0 }
        if (a != b) return a - b
    }
    return 0
}
