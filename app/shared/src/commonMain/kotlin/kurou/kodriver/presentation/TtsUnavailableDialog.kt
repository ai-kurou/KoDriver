package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.tts_continue
import kurou.kodriver.app.shared.generated.resources.tts_engine_action
import kurou.kodriver.app.shared.generated.resources.tts_engine_body
import kurou.kodriver.app.shared.generated.resources.tts_engine_title
import kurou.kodriver.app.shared.generated.resources.tts_language_action
import kurou.kodriver.app.shared.generated.resources.tts_language_body
import kurou.kodriver.app.shared.generated.resources.tts_language_title
import kurou.kodriver.app.shared.generated.resources.tts_windows_action
import kurou.kodriver.app.shared.generated.resources.tts_windows_body
import kurou.kodriver.app.shared.generated.resources.tts_windows_restart
import kurou.kodriver.app.shared.generated.resources.tts_windows_title
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TtsUnavailableDialog(
    reason: TtsUnavailableGuidance,
    onPrimaryClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val (title, body, action) =
        when (reason) {
            TtsUnavailableGuidance.EngineMissing -> {
                Triple(Res.string.tts_engine_title, Res.string.tts_engine_body, Res.string.tts_engine_action)
            }

            TtsUnavailableGuidance.LanguageDataMissing -> {
                Triple(Res.string.tts_language_title, Res.string.tts_language_body, Res.string.tts_language_action)
            }

            TtsUnavailableGuidance.WindowsSpeechUnavailable -> {
                Triple(Res.string.tts_windows_title, Res.string.tts_windows_body, Res.string.tts_windows_action)
            }
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
                if (reason == TtsUnavailableGuidance.WindowsSpeechUnavailable) {
                    Text(stringResource(Res.string.tts_windows_restart), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { Button(onClick = onPrimaryClick) { Text(stringResource(action)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.tts_continue)) } },
    )
}
