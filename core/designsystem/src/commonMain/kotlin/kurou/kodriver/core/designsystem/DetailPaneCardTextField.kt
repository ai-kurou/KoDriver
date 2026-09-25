package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
 * 入力途中の値はこの Composable 内のローカル状態として保持し、フォーカスが外れたとき・
 * ソフトウェアキーボードの完了操作のとき・この Composable がコンポジションから破棄されるとき
 * （画面遷移などで [value] が確定済みの値のまま消えるとき）に [onValueChangeFinished] で確定する。
 * 破棄時の確定を入れているのは、フォーカスが外れないまま（Doneキーや再生ボタンも押さないまま）
 * 別ペインへ切り替える・アプリを閉じるといった操作をした場合、`onFocusChanged` はフォーカス喪失として
 * 呼ばれない（ノードごと破棄されるだけ）ため、確定を破棄時にも行わないと入力内容が保存されずに失われるため。
 *
 * 末尾の再生ボタンは、入力中の文言（空欄なら既定の文言）の試聴に使う。
 *
 * [selected] が true のときは、同じ [DetailPaneCard] 内に並ぶ [DetailPaneCardChips] の選択済みチップと同じく
 * チェックアイコンとプライマリ色のインジケーターを表示し、「いまはこちらが読み上げに使われる」ことを示す。
 * チップ側と [selected] を排他にして渡すことで、どちらが使われるかを一目で判別できるようにする。
 * [selected] の判定は永続化された確定値ではなく、[onTextChanged] で通知される入力中の文字列に基づかせる想定。
 * こうすることで、フォーカスを外す・再生ボタンを押すといった確定操作を待たずに、1文字入力した時点で
 * 選択状態の見た目が切り替わる（永続化自体は [onValueChangeFinished] のタイミングのまま変えない）。
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
    selected: Boolean = false,
    supportingText: String? = null,
    previewContentDescription: String? = null,
    selectedContentDescription: String? = null,
    onTextChanged: (String) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    var text by remember(value) { mutableStateOf(value) }

    // 破棄時にも最新の入力内容・コールバックで確定できるよう、DisposableEffect の onDispose から
    // 参照する text と onValueChangeFinished は rememberUpdatedState で常に最新のものを使う。
    val latestText by rememberUpdatedState(text)
    val latestOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
    DisposableEffect(Unit) {
        onDispose { latestOnValueChangeFinished(latestText) }
    }

    TextField(
        value = text,
        onValueChange = { input ->
            text = input.replace("\n", "").take(maxLength)
            onTextChanged(text)
        },
        enabled = enabled,
        placeholder = { Text(text = placeholder) },
        supportingText = supportingText?.let { { Text(text = it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onValueChangeFinished(text) }),
        colors =
            if (selected) {
                TextFieldDefaults.colors(
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            } else {
                TextFieldDefaults.colors()
            },
        leadingIcon =
            if (selected) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = selectedContentDescription,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                null
            },
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
            DetailPaneCard(
                title = "イエローフラッグ",
                checked = true,
                onCheckedChange = {},
                modifier = Modifier.padding(KoDriverSpacing.large),
                bottomContent = {
                    DetailPaneCardChips(
                        chipLabels = listOf("イエローフラッグ"),
                        selectedChipLabels = emptySet(),
                        chipEnabled = true,
                        onChipClick = {},
                    )
                    DetailPaneCardTextField(
                        value = "イエロー、前方注意",
                        placeholder = "イエローフラッグ",
                        maxLength = 30,
                        onValueChangeFinished = {},
                        onPreviewClick = {},
                        selected = true,
                        supportingText = "この文言を音声合成で読み上げます（収録音声は使いません）",
                    )
                },
            )
        }
    }
}
