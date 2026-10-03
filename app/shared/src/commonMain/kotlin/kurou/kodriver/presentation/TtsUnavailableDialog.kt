package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import kurou.kodriver.feature.otherlist.TextToSpeechUnavailableReason
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TtsUnavailableDialog(
    reason: TextToSpeechUnavailableReason,
    onPrimaryClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val (title, body, action) =
        when (reason) {
            TextToSpeechUnavailableReason.EngineMissing -> {
                Triple(Res.string.tts_engine_title, Res.string.tts_engine_body, Res.string.tts_engine_action)
            }

            TextToSpeechUnavailableReason.LanguageDataMissing -> {
                Triple(Res.string.tts_language_title, Res.string.tts_language_body, Res.string.tts_language_action)
            }

            TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> {
                Triple(Res.string.tts_windows_title, Res.string.tts_windows_body, Res.string.tts_windows_action)
            }
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
                if (reason == TextToSpeechUnavailableReason.WindowsSpeechUnavailable) {
                    Text(stringResource(Res.string.tts_windows_restart), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { Button(onClick = onPrimaryClick) { Text(stringResource(action)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.tts_continue)) } },
    )
}

/** 閉じた案内はこの起動中には再表示せず、設定への移動では閉じない。 */
@Composable
internal fun TtsUnavailableDialogHost(
    reason: TextToSpeechUnavailableReason?,
    onInstallEngine: () -> Unit,
    onOpenLanguageSettings: () -> Unit,
    onOpenWindowsSpeechSettings: () -> Unit,
) {
    var dismissed by rememberSaveable { mutableStateOf(false) }
    if (reason != null && !dismissed) {
        TtsUnavailableDialog(
            reason = reason,
            onPrimaryClick =
                when (reason) {
                    TextToSpeechUnavailableReason.EngineMissing -> onInstallEngine
                    TextToSpeechUnavailableReason.LanguageDataMissing -> onOpenLanguageSettings
                    TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> onOpenWindowsSpeechSettings
                },
            onDismiss = { dismissed = true },
        )
    }
}
