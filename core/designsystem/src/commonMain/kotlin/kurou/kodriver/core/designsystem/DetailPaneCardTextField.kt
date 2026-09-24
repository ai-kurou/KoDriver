package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * DetailPaneCard の bottomContent で、読み上げ文言などをユーザーが入力するための TextField を提供する公開関数。
 *
 * [value] が空のときは [placeholder]（既定の読み上げ文言）をプレースホルダーとして表示し、
 * 「空欄なら既定の音声で読み上げる」という状態をそのまま画面上で表現する。
 * 入力途中の値はこの Composable 内のローカル状態として保持し、フォーカスが外れたときと
 * ソフトウェアキーボードの完了操作のときに [onValueChangeFinished] で確定する（[ThresholdSlider] と同じ方針）。
 *
 * 末尾の再生ボタンは、入力中の文言（空欄なら既定の文言）の試聴に使う。
 */
@Suppress("LongParameterList")
@Composable
fun DetailPaneCardTextField(
    value: String,
    placeholder: String,
    maxLength: Int,
    onValueChangeFinished: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    previewContentDescription: String? = null,
) {
    val haptic = LocalHapticFeedback.current
    var text by remember(value) { mutableStateOf(value) }

    TextField(
        value = text,
        onValueChange = { input -> text = input.replace("\n", "").take(maxLength) },
        enabled = enabled,
        placeholder = { Text(text = placeholder) },
        supportingText = supportingText?.let { { Text(text = it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onValueChangeFinished(text) }),
        trailingIcon = {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onValueChangeFinished(text)
                    onPreviewClick(text)
                },
                enabled = enabled,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = previewContentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        modifier =
            modifier
                .fillMaxWidth()
                .onFocusChanged { focusState -> if (!focusState.isFocused) onValueChangeFinished(text) },
    )
}

@Preview(showBackground = true)
@Composable
private fun DetailPaneCardTextFieldPreview() {
    KoDriverTheme {
        Column {
            DetailPaneCard(
                title = "イエローフラッグ",
                checked = true,
                onCheckedChange = {},
                modifier = Modifier.padding(KoDriverSpacing.large),
                bottomContent = {
                    DetailPaneCardChips(
                        chipLabels = listOf("イエローフラッグ"),
                        selectedChipLabels = setOf("イエローフラッグ"),
                        chipEnabled = true,
                        onChipClick = {},
                    )
                    DetailPaneCardTextField(
                        value = "",
                        placeholder = "イエローフラッグ",
                        maxLength = 30,
                        onValueChangeFinished = {},
                        onPreviewClick = {},
                        supportingText = "空欄のままなら収録音声で読み上げます",
                    )
                },
            )
        }
    }
}
