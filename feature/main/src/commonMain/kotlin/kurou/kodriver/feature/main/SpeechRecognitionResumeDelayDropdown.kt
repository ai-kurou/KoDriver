package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.speech_recognition_resume_delay_label
import kurou.kodriver.feature.main.generated.resources.speech_recognition_resume_delay_option
import org.jetbrains.compose.resources.stringResource

/** 読み上げが終わってから聞き取りを再開するまでの待ち時間を選ぶドロップダウン。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SpeechRecognitionResumeDelayDropdown(
    selectedMs: Int,
    onSelected: (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(Res.string.speech_recognition_resume_delay_option, selectedMs.toSecondsText()),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(Res.string.speech_recognition_resume_delay_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier =
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag("speech_recognition_resume_delay"),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SPEECH_RECOGNITION_RESUME_DELAY_OPTIONS_MS.forEach { delayMs ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.speech_recognition_resume_delay_option, delayMs.toSecondsText()))
                    },
                    modifier = Modifier.testTag("speech_recognition_resume_delay_$delayMs"),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onSelected(delayMs)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** ミリ秒を「0.5」のような秒の文字列にする（小数第1位まで。ロケールに依存させない）。 */
internal fun Int.toSecondsText(): String = "${this / 1_000}.${this % 1_000 / 100}"
