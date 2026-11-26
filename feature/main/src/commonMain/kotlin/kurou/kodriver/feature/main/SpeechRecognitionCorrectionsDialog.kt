package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import kurou.kodriver.core.designsystem.KoDriverDialogTextButton
import kurou.kodriver.core.designsystem.KoDriverPhraseChip
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.speech_recognition_corrections_arrow
import kurou.kodriver.feature.main.generated.resources.speech_recognition_corrections_body
import kurou.kodriver.feature.main.generated.resources.speech_recognition_corrections_close
import kurou.kodriver.feature.main.generated.resources.speech_recognition_corrections_title
import org.jetbrains.compose.resources.stringResource

/**
 * 音声認識が取り違えやすい語と、置き換え先の正しい語（[corrections]）の一覧を見せるダイアログ。
 * 認識結果を読み上げ項目の語句と照合する前に自動で行う置き換えの説明で、音声認識カードの ⓘ から開く。
 */
@Composable
internal fun SpeechRecognitionCorrectionsDialog(
    corrections: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // AlertDialog の内容は別ウィンドウで組み立てられるため、外側で読んだハプティックを部品へ渡す。
    val haptic = LocalHapticFeedback.current
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(Res.string.speech_recognition_corrections_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                Text(
                    text = stringResource(Res.string.speech_recognition_corrections_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                    items(corrections) { (wrong, right) -> CorrectionRow(wrong, right) }
                }
            }
        },
        confirmButton = {
            KoDriverDialogTextButton(
                text = stringResource(Res.string.speech_recognition_corrections_close),
                onClick = onDismiss,
                hapticFeedback = haptic,
            )
        },
    )
}

@Composable
private fun CorrectionRow(
    wrong: String,
    right: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small, Alignment.CenterHorizontally),
    ) {
        KoDriverPhraseChip(text = wrong, muted = true)
        Text(
            text = stringResource(Res.string.speech_recognition_corrections_arrow),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = right,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = KoDriverSpacing.extraSmall),
        )
    }
}
