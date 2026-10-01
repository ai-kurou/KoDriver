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
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneCardTextField
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_custom_text_kept
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_custom_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_custom_text_selected
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_custom_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_custom_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_custom_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_description
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_red
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_session_stop
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
        onPreviewClicked = viewModel::onPreviewClicked,
        onRecordedVoiceSelected = viewModel::onRecordedVoiceSelected,
        onRedFlagVoiceTypeChanged = viewModel::onRedFlagVoiceTypeChanged,
        onRedFlagPreviewClicked = viewModel::onRedFlagPreviewClicked,
        onFlagTextChanged = viewModel::onFlagTextChanged,
        onFlagTextPreviewClicked = viewModel::onFlagTextPreviewClicked,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsReadoutFlagDetailPaneContent(
    uiState: LmuWindowsReadoutFlagDetailUiState,
    onFlagEnabledChanged: (FlagReadoutItem, Boolean) -> Unit,
    onPreviewClicked: (FlagReadoutItem) -> Unit,
    onRecordedVoiceSelected: (FlagReadoutItem, () -> Unit) -> Unit,
    onRedFlagVoiceTypeChanged: (RedFlagVoiceType) -> Unit,
    onRedFlagPreviewClicked: (RedFlagVoiceType) -> Unit,
    onFlagTextChanged: (FlagReadoutItem, String) -> Unit,
    onFlagTextPreviewClicked: (FlagReadoutItem, String) -> Unit,
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
            val chips =
                flagChips(
                    item = item,
                    onPreviewClicked = onPreviewClicked,
                    onRedFlagVoiceTypeChanged = onRedFlagVoiceTypeChanged,
                    onRedFlagPreviewClicked = onRedFlagPreviewClicked,
                )
            FlagReadoutCard(
                item = item,
                checked = uiState.enabledStates[item.key] ?: true,
                text = uiState.flagText(item),
                customTextSelected = uiState.isCustomTextSelected(item),
                chips = chips,
                selectedChip = chips.getOrNull(if (item.isSessionStopSelected(uiState.redFlagVoiceType)) 1 else 0),
                isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                onCheckedChange = { enabled -> onFlagEnabledChanged(item, enabled) },
                onRecordedVoiceSelected = { preview -> onRecordedVoiceSelected(item, preview) },
                onTextChanged = { text -> onFlagTextChanged(item, text) },
                onTextPreviewClick = { text -> onFlagTextPreviewClicked(item, text) },
            )
        }
    }
}

/** 収録音声のチップ1つ分の表示ラベルと、タップ時の処理。 */
private data class FlagChip(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * [item] の収録音声のチップ一覧。
 * レッドフラッグは音声種別（RedFlag / SessionStop）ごとに2つ、それ以外のフラッグは自由文字列のみのため0個。
 */
@Composable
private fun flagChips(
    item: FlagReadoutItem,
    onPreviewClicked: (FlagReadoutItem) -> Unit,
    onRedFlagVoiceTypeChanged: (RedFlagVoiceType) -> Unit,
    onRedFlagPreviewClicked: (RedFlagVoiceType) -> Unit,
): List<FlagChip> {
    if (!item.recordedVoiceSelectable) return emptyList()
    if (item != FlagReadoutItem.RedFlag) {
        return listOf(FlagChip(stringResource(item.chipLabelRes)) { onPreviewClicked(item) })
    }
    return listOf(
        FlagChip(stringResource(Res.string.flag_red)) {
            onRedFlagVoiceTypeChanged(RedFlagVoiceType.RED_FLAG)
            onRedFlagPreviewClicked(RedFlagVoiceType.RED_FLAG)
        },
        FlagChip(stringResource(Res.string.flag_session_stop)) {
            onRedFlagVoiceTypeChanged(RedFlagVoiceType.SESSION_STOP)
            onRedFlagPreviewClicked(RedFlagVoiceType.SESSION_STOP)
        },
    )
}

/** [voiceType] のうち、[chips] の2番目（セッションストップ）が選択中かどうか。レッドフラッグ以外は常に false。 */
private fun FlagReadoutItem.isSessionStopSelected(voiceType: RedFlagVoiceType): Boolean =
    this == FlagReadoutItem.RedFlag && voiceType == RedFlagVoiceType.SESSION_STOP

/**
 * 収録音声のチップとカスタム文言の入力欄を持つフラッグ項目のカード。
 * [chips] が空の項目（レッドフラッグ以外）はチップを表示せず、カスタム文言の入力欄のみを表示する。
 *
 * 収録音声のチップとカスタム文言の選択状態は排他にして、どちらが使われるかを明示する。
 * 選択状態は保存済みの文言と収録音声の選択設定から決まる（[customTextSelected]）。
 * チップを選んでも文言は消さず、文言を編集し直すとカスタム文言の選択に戻る。
 */
@Suppress("LongParameterList", "UnstableCollections")
@Composable
private fun FlagReadoutCard(
    item: FlagReadoutItem,
    checked: Boolean,
    text: String,
    customTextSelected: Boolean,
    chips: List<FlagChip>,
    selectedChip: FlagChip?,
    isTextToSpeechAvailable: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onRecordedVoiceSelected: (preview: () -> Unit) -> Unit,
    onTextChanged: (String) -> Unit,
    onTextPreviewClick: (String) -> Unit,
) {
    DetailPaneCard(
        title = stringResource(item.labelRes),
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        bottomContent = {
            if (selectedChip != null) {
                DetailPaneCardChips(
                    chipLabels = chips.map { it.label },
                    selectedChipLabels = if (customTextSelected) emptySet() else setOf(selectedChip.label),
                    chipEnabled = true,
                    onChipClick = { label ->
                        // 収録音声のチップを選び直す操作。入力済みのカスタム文言は消さず、収録音声を使う設定にする。
                        // 試聴は保存済みの選択状態を参照するため、保存の完了後に行う。
                        onRecordedVoiceSelected { chips.first { it.label == label }.onClick() }
                    },
                )
            }
            DetailPaneCardTextField(
                value = text,
                placeholder = selectedChip?.label ?: stringResource(item.labelRes),
                maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                onValueChangeFinished = onTextChanged,
                onPreviewClick = onTextPreviewClick,
                enabled = isTextToSpeechAvailable,
                selected = customTextSelected,
                supportingText =
                    when {
                        !isTextToSpeechAvailable -> stringResource(Res.string.flag_custom_text_unavailable)
                        customTextSelected -> stringResource(Res.string.flag_custom_text_selected)
                        text.isNotEmpty() -> stringResource(Res.string.flag_custom_text_kept)
                        else -> stringResource(Res.string.flag_custom_text_supporting)
                    },
                previewContentDescription = stringResource(Res.string.flag_custom_text_preview),
                selectedContentDescription = stringResource(Res.string.flag_custom_text_selected_icon),
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
                    redFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
                ),
            onFlagEnabledChanged = { _, _ -> },
            onPreviewClicked = {},
            onRecordedVoiceSelected = { _, _ -> },
            onRedFlagVoiceTypeChanged = {},
            onRedFlagPreviewClicked = {},
            onFlagTextChanged = { _, _ -> },
            onFlagTextPreviewClicked = { _, _ -> },
        )
    }
}
