package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardTextField
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_description
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_text_unavailable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * LmuWindowsReadoutFlagDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutFlagDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutFlagDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutFlagDetailPaneContent(
        uiState = uiState,
        onFlagEnabledChanged = viewModel::onFlagEnabledChanged,
        onFlagTextChanged = viewModel::onFlagTextChanged,
        onFlagTextReset = viewModel::onFlagTextReset,
        onFlagTextPreviewClicked = viewModel::onFlagTextPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutFlagDetailPaneContent(
    uiState: LmuWindowsReadoutFlagDetailUiState,
    onFlagEnabledChanged: (FlagReadoutItem, Boolean) -> Unit,
    onFlagTextChanged: (FlagReadoutItem, String) -> Unit,
    onFlagTextReset: (FlagReadoutItem) -> Unit,
    onFlagTextPreviewClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.flag_description),
        )
        FlagReadoutItem.entries.forEach { item ->
            FlagReadoutCard(
                item = item,
                checked = uiState.enabledStates[item.key] ?: true,
                text = uiState.flagText(item),
                isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                onCheckedChange = { enabled -> onFlagEnabledChanged(item, enabled) },
                onTextChanged = { text -> onFlagTextChanged(item, text) },
                onTextPreviewClick = onFlagTextPreviewClicked,
                onFlagTextReset = { onFlagTextReset(item) },
            )
        }
    }
}

/** 自由文字列の入力と試聴を提供するフラッグ項目のカード。 */
@Composable
private fun FlagReadoutCard(
    item: FlagReadoutItem,
    checked: Boolean,
    text: String,
    isTextToSpeechAvailable: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTextChanged: (String) -> Unit,
    onTextPreviewClick: (String) -> Unit,
    onFlagTextReset: () -> Unit,
) {
    val textState = rememberPendingText(text, READOUT_CUSTOM_TEXT_MAX_LENGTH)
    val currentText = textState.currentText
    val changeText: (String) -> Unit = {
        textState.change(it)
        onTextChanged(it)
    }
    DetailPaneCard(
        title = stringResource(item.labelRes),
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        bottomContent = {
            DetailPaneCardTextField(
                value = currentText,
                defaultValue = item.defaultText,
                onResetToDefault = {
                    textState.change(item.defaultText)
                    onFlagTextReset()
                },
                resetContentDescription = stringResource(Res.string.flag_text_reset_to_default),
                placeholder = stringResource(item.labelRes),
                maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                onValueChangeFinished = changeText,
                onPreviewClick = onTextPreviewClick,
                enabled = isTextToSpeechAvailable,
                selected = currentText.isNotBlank(),
                supportingText =
                    when {
                        !isTextToSpeechAvailable -> stringResource(Res.string.flag_text_unavailable)
                        currentText.isNotBlank() -> null
                        else -> stringResource(Res.string.flag_text_supporting)
                    },
                previewContentDescription = stringResource(Res.string.flag_text_preview),
                selectedContentDescription = stringResource(Res.string.flag_text_selected_icon),
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutFlagDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutFlagDetailPaneContent(
            uiState =
                LmuWindowsReadoutFlagDetailUiState(
                    enabledStates =
                        mapOf(
                            ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                            ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                            ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                            ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
                        ),
                ),
            onFlagEnabledChanged = { _, _ -> },
            onFlagTextChanged = { _, _ -> },
            onFlagTextReset = {},
            onFlagTextPreviewClicked = {},
        )
    }
}
