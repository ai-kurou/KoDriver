package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * DetailPaneCard の bottomContent で、読み上げ文言などをユーザーが入力するための TextField を提供する公開関数。
 *
 * [value] が空のときは [placeholder]（既定の読み上げ文言）をプレースホルダーとして表示し、
 * 「空欄なら既定の音声で読み上げる」という状態をそのまま画面上で表現する。
 * 入力途中の値はこの Composable 内のローカル状態として保持しつつ、1文字入力するたびに
 * [onValueChangeFinished] で即座に確定（永続化）する。フォーカス喪失・Doneキー・アプリ終了といった
 * 特定のイベントを待って確定する設計にすると、ソフトウェアキーボードを開いたままアプリを終了する等、
 * そのイベントが発生しない操作をされた場合に入力内容が保存されないまま失われる（実際に発生した不具合）。
 * 1文字ごとに確定することでこの問題を避ける。
 *
 * 末尾の再生ボタンは、入力中の文言（空欄なら既定の文言）の試聴に使う。
 *
 * [selected] が true のときは、同じ [DetailPaneCard] 内に並ぶ [DetailPaneCardChips] の選択済みチップと同じく
 * チェックアイコンとプライマリ色のインジケーターを表示し、「いまはこちらが読み上げに使われる」ことを示す。
 * チップ側と [selected] を排他にして渡すことで、どちらが使われるかを一目で判別できるようにする。
 * [selected] の判定は [onValueChangeFinished] で通知される文字列に基づかせる想定（1文字入力するたびに更新される）。
 *
 * [supportingText] の右側に、入力中の文字数と [maxLength] を「12/30」の形式で常に表示する。
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
) {
    val haptic = LocalHapticFeedback.current
    var text by remember(value) { mutableStateOf(value) }

    TextField(
        value = text,
        onValueChange = { input ->
            text = input.replace("\n", "").take(maxLength)
            onValueChangeFinished(text)
        },
        enabled = enabled,
        placeholder = { Text(text = placeholder) },
        supportingText = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                supportingText?.let { Text(text = it, modifier = Modifier.weight(1f, fill = false)) }
                Text(text = "${text.length}/$maxLength")
            }
        },
        singleLine = true,
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
        modifier = modifier.fillMaxWidth(),
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
