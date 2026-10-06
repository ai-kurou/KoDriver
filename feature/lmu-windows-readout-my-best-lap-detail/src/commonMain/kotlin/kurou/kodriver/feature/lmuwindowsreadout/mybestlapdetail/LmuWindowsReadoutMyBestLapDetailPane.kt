package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownLmuWindowsMyBestLapReadoutPlaceholders
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_description
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_enabled
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_laptime_insert
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_laptime_placeholder_hint
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_text_label
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.generated.resources.my_best_lap_text_unknown_placeholders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * LmuWindowsReadoutMyBestLapDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutMyBestLapDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutMyBestLapDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutMyBestLapDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsReadoutMyBestLapDetailPaneContent(
    uiState: LmuWindowsReadoutMyBestLapDetailUiState = LmuWindowsReadoutMyBestLapDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.my_best_lap_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.my_best_lap_enabled),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MyBestLapReadoutField(
                        text = uiState.readoutText,
                        available = uiState.isTextToSpeechAvailable,
                        onTextChanged = onReadoutTextChanged,
                        onPreviewClick = onReadoutTextPreviewClicked,
                    )
                }
            },
        )
    }
}

@Composable
private fun MyBestLapReadoutField(
    text: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    var currentText by remember { mutableStateOf(text) }
    // 保存が非同期のため、入力中の最新の値と一致するまでは保存済みの古い値で入力欄を巻き戻さない
    var pendingText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(text, pendingText) {
        if (pendingText == null) {
            currentText = text
        } else if (pendingText == text) {
            // 保存値は trim と文字数制限で正規化されるため、入力欄は巻き戻さず待機状態だけ解除する
            pendingText = null
        }
    }
    val changeText: (String) -> Unit = {
        currentText = it
        pendingText = it.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        onTextChanged(it)
    }
    ReadoutTextField(
        label = stringResource(Res.string.my_best_lap_text_label),
        text = currentText,
        defaultText = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
        isTextToSpeechAvailable = available,
        onTextChanged = changeText,
        onPreviewClick = onPreviewClick,
        unknownPlaceholders =
            findUnknownLmuWindowsMyBestLapReadoutPlaceholders(currentText)
                .joinToString("、"),
    )
    Row(
        modifier = Modifier.padding(bottom = KoDriverSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        AssistChip(
            onClick = {
                changeText(
                    currentText + LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
                )
            },
            label = { Text(stringResource(Res.string.my_best_lap_laptime_insert)) },
            enabled =
                available &&
                    currentText.length + LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER.length <=
                    READOUT_CUSTOM_TEXT_MAX_LENGTH,
        )
        Text(
            text = stringResource(Res.string.my_best_lap_laptime_placeholder_hint),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 自己ベストラップ更新の文言入力と試聴を提供する。 */
@Suppress("LongParameterList")
@Composable
private fun ReadoutTextField(
    label: String,
    text: String,
    defaultText: String,
    isTextToSpeechAvailable: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
    unknownPlaceholders: String = "",
) {
    DetailPaneLabeledTextField(
        label = label,
        value = text,
        defaultValue = defaultText,
        onResetToDefault = { onTextChanged(defaultText) },
        resetContentDescription = stringResource(Res.string.my_best_lap_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> {
                    stringResource(Res.string.my_best_lap_text_unavailable)
                }

                unknownPlaceholders.isNotEmpty() -> {
                    stringResource(
                        Res.string.my_best_lap_text_unknown_placeholders,
                        unknownPlaceholders,
                    )
                }

                text.isNotBlank() -> {
                    null
                }

                else -> {
                    stringResource(Res.string.my_best_lap_text_supporting)
                }
            },
        previewContentDescription = stringResource(Res.string.my_best_lap_text_preview),
        selectedContentDescription = stringResource(Res.string.my_best_lap_text_selected_icon),
    )
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutMyBestLapDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutMyBestLapDetailPaneContent()
    }
}
