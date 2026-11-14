package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview

/**
 * 保存待ちの入力状態と、プレースホルダーの挿入チップを備えた読み上げ文言入力欄。
 * 挿入チップの下にヒントを全幅で縦に並べる。文字列は呼び出し側で解決し、[unknownPlaceholdersText] は保存前の現在の入力から生成する。
 */
@Suppress("LongParameterList")
@Composable
fun PlaceholderReadoutTextField(
    label: String,
    savedText: String,
    defaultText: String,
    maxLength: Int,
    isTextToSpeechAvailable: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
    placeholder: String,
    insertLabel: String,
    placeholderHint: String,
    resetContentDescription: String,
    previewContentDescription: String,
    selectedContentDescription: String,
    unavailableText: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    unknownPlaceholdersText: @Composable (currentText: String) -> String? = { null },
) {
    val textState = rememberPendingText(savedText, maxLength)
    val currentText = textState.currentText
    val insertState = rememberTextInsertState()
    val haptic = LocalHapticFeedback.current
    Column(modifier = modifier) {
        ReadoutTextField(
            label = label,
            text = currentText,
            defaultText = defaultText,
            maxLength = maxLength,
            isTextToSpeechAvailable = isTextToSpeechAvailable,
            onTextChanged = {
                textState.change(it)
                onTextChanged(it)
            },
            onPreviewClick = onPreviewClick,
            insertState = insertState,
            resetContentDescription = resetContentDescription,
            previewContentDescription = previewContentDescription,
            selectedContentDescription = selectedContentDescription,
            unavailableText = unavailableText,
            supportingText = supportingText,
            unknownPlaceholdersText = unknownPlaceholdersText(currentText),
        )
        Column(
            modifier = Modifier.padding(bottom = KoDriverSpacing.small),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
        ) {
            AssistChip(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    insertState.insert(placeholder)
                },
                label = { Text(insertLabel) },
                enabled = isTextToSpeechAvailable && currentText.length + placeholder.length <= maxLength,
            )
            Text(
                text = placeholderHint,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderReadoutTextFieldPreview() {
    KoDriverTheme {
        PlaceholderReadoutTextField(
            label = "読み上げ文言",
            savedText = "",
            defaultText = "ベストラップは{laptime}です",
            maxLength = 100,
            isTextToSpeechAvailable = true,
            onTextChanged = {},
            onPreviewClick = {},
            placeholder = "{laptime}",
            insertLabel = "ラップタイムを挿入",
            placeholderHint = "ラップタイムに置き換えます",
            resetContentDescription = "デフォルトに戻す",
            previewContentDescription = "試聴",
            selectedContentDescription = "選択中",
            unavailableText = "読み上げを利用できません",
            supportingText = "空欄のままなら読み上げません",
        )
    }
}
