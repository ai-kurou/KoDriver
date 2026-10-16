package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * 読み上げ文言の入力・試聴・既定値へのリセットを提供する。
 * 文言と [maxLength] は呼び出し側で解決して渡す。
 *
 * 補足文は TTS 不可、空でない [unknownPlaceholdersText]、空欄時の [supportingText] の順に優先する。
 * [text] が空白のみでなければ選択アイコンを表示し、警告がなければ補足文を非表示にする。
 * リセット時は [defaultText] を [onTextChanged] に通知する。
 */
@Suppress("LongParameterList")
@Composable
fun ReadoutTextField(
    label: String,
    text: String,
    defaultText: String,
    maxLength: Int,
    isTextToSpeechAvailable: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
    insertState: TextInsertState,
    resetContentDescription: String,
    previewContentDescription: String,
    selectedContentDescription: String,
    unavailableText: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    unknownPlaceholdersText: String? = null,
) {
    DetailPaneLabeledTextField(
        label = label,
        value = text,
        defaultValue = defaultText,
        onResetToDefault = { onTextChanged(defaultText) },
        resetContentDescription = resetContentDescription,
        maxLength = maxLength,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        insertState = insertState,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> unavailableText
                !unknownPlaceholdersText.isNullOrEmpty() -> unknownPlaceholdersText
                text.isNotBlank() -> null
                else -> supportingText
            },
        previewContentDescription = previewContentDescription,
        selectedContentDescription = selectedContentDescription,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun ReadoutTextFieldPreview() {
    KoDriverTheme {
        ReadoutTextField(
            label = "読み上げ文言",
            text = "",
            defaultText = "残量を確認してください",
            maxLength = 30,
            isTextToSpeechAvailable = true,
            onTextChanged = {},
            onPreviewClick = {},
            insertState = rememberTextInsertState(),
            resetContentDescription = "デフォルトに戻す",
            previewContentDescription = "試聴",
            selectedContentDescription = "選択中",
            unavailableText = "読み上げを利用できません",
            supportingText = "空欄のままなら読み上げません",
        )
    }
}
