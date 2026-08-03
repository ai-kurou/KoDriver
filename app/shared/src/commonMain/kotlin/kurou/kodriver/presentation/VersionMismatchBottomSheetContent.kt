package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.version_mismatch_app_version
import kurou.kodriver.app.shared.generated.resources.version_mismatch_body
import kurou.kodriver.app.shared.generated.resources.version_mismatch_close
import kurou.kodriver.app.shared.generated.resources.version_mismatch_title
import kurou.kodriver.app.shared.generated.resources.version_mismatch_update_app
import kurou.kodriver.app.shared.generated.resources.version_mismatch_update_windows
import kurou.kodriver.app.shared.generated.resources.version_mismatch_windows_version
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VersionMismatchBottomSheetContent(
    windowsKoDriverVersion: String,
    appVersion: String,
    onDismiss: () -> Unit,
) {
    val windowsIsNewer = parseVersion(windowsKoDriverVersion) > parseVersion(appVersion)

    Column(
        modifier =
            Modifier
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
            text =
                if (windowsIsNewer) {
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

private fun parseVersion(version: String): List<Int> = version.split(".").map { it.toIntOrNull() ?: 0 }

private operator fun List<Int>.compareTo(other: List<Int>): Int {
    val size = maxOf(this.size, other.size)
    for (i in 0 until size) {
        val a = this.getOrElse(i) { 0 }
        val b = other.getOrElse(i) { 0 }
        if (a != b) return a - b
    }
    return 0
}
