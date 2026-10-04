package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * DetailPaneCard の bottomContent で、ラベル付きの読み上げ文言入力欄を提供する公開関数。
 *
 * [label] を [DetailPaneCardTextField] の上に表示し、両者の間隔を [KoDriverSpacing.extraSmall] にする。
 * 同じカード内に複数の入力欄を並べるときは、呼び出し側でこの関数同士の間隔（[KoDriverSpacing.large] 想定）を付ける。
 * 入力・確定・試聴・リセット・選択状態の挙動は [DetailPaneCardTextField] と同じ。[label] はプレースホルダーにも使う。
 */
@Composable
fun DetailPaneLabeledTextField(
    label: String,
    value: String,
    maxLength: Int,
    onValueChangeFinished: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    supportingText: String? = null,
    previewContentDescription: String? = null,
    selectedContentDescription: String? = null,
    defaultValue: String? = null,
    onResetToDefault: (() -> Unit)? = null,
    resetContentDescription: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        DetailPaneCardTextField(
            value = value,
            placeholder = label,
            maxLength = maxLength,
            onValueChangeFinished = onValueChangeFinished,
            onPreviewClick = onPreviewClick,
            enabled = enabled,
            selected = selected,
            supportingText = supportingText,
            previewContentDescription = previewContentDescription,
            selectedContentDescription = selectedContentDescription,
            defaultValue = defaultValue,
            onResetToDefault = onResetToDefault,
            resetContentDescription = resetContentDescription,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailPaneLabeledTextFieldPreview() {
    KoDriverTheme {
        DetailPaneLabeledTextField(
            label = "左側の読み上げ",
            value = "",
            maxLength = 30,
            onValueChangeFinished = {},
            onPreviewClick = {},
            supportingText = "空欄のままなら読み上げません",
        )
    }
}
