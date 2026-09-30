package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        onRedFlagEnabledChanged = viewModel::onRedFlagEnabledChanged,
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
    onRedFlagEnabledChanged: (Boolean) -> Unit,
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
            val chipLabel = stringResource(item.chipLabelRes)
            val checked = uiState.enabledStates[item.key] ?: true
            val flagText = uiState.flagText(item)
            // 収録音声のチップとカスタム文言の入力欄の選択状態を排他にして、どちらが使われるかを明示するための状態。
            // DetailPaneCardTextField は1文字入力するたびに onValueChangeFinished（= onFlagTextChanged）で
            // 即座に確定するため、この値をそのまま選択状態の基準にできる。
            var hasText by remember(item) { mutableStateOf(flagText.isNotEmpty()) }
            LaunchedEffect(flagText) {
                hasText = flagText.isNotEmpty()
            }
            val customTextSelected = hasText
            DetailPaneCard(
                title = stringResource(item.labelRes),
                checked = checked,
                onCheckedChange = { enabled -> onFlagEnabledChanged(item, enabled) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
                bottomContent = {
                    DetailPaneCardChips(
                        chipLabels = listOf(chipLabel),
                        selectedChipLabels = if (customTextSelected) emptySet() else setOf(chipLabel),
                        chipEnabled = true,
                        onChipClick = {
                            // 収録音声のチップを選び直す操作なので、入力済みのカスタム文言はクリアする。
                            if (customTextSelected) {
                                hasText = false
                                onFlagTextChanged(item, "")
                            }
                            onPreviewClicked(item)
                        },
                    )
                    DetailPaneCardTextField(
                        value = flagText,
                        placeholder = chipLabel,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        onValueChangeFinished = { text ->
                            hasText = text.isNotEmpty()
                            onFlagTextChanged(item, text)
                        },
                        onPreviewClick = { text -> onFlagTextPreviewClicked(item, text) },
                        enabled = uiState.isTextToSpeechAvailable,
                        selected = customTextSelected,
                        supportingText =
                            when {
                                !uiState.isTextToSpeechAvailable -> {
                                    stringResource(Res.string.flag_custom_text_unavailable)
                                }

                                customTextSelected -> {
                                    stringResource(Res.string.flag_custom_text_selected)
                                }

                                else -> {
                                    stringResource(Res.string.flag_custom_text_supporting)
                                }
                            },
                        previewContentDescription = stringResource(Res.string.flag_custom_text_preview),
                        selectedContentDescription = stringResource(Res.string.flag_custom_text_selected_icon),
                    )
                },
            )
        }
        val redFlagLabel = stringResource(Res.string.flag_red)
        val sessionStopLabel = stringResource(Res.string.flag_session_stop)
        val redFlagChecked = uiState.enabledStates[ReadoutItemKey.LmuWindows.Flag.RedFlag] ?: true
        val selectedRedFlagLabel =
            when (uiState.redFlagVoiceType) {
                RedFlagVoiceType.RED_FLAG -> redFlagLabel
                RedFlagVoiceType.SESSION_STOP -> sessionStopLabel
            }
        DetailPaneCard(
            title = redFlagLabel,
            checked = redFlagChecked,
            onCheckedChange = onRedFlagEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = listOf(redFlagLabel, sessionStopLabel),
                    selectedChipLabels = setOf(selectedRedFlagLabel),
                    chipEnabled = true,
                    onChipClick = { label ->
                        val type =
                            if (label == redFlagLabel) {
                                RedFlagVoiceType.RED_FLAG
                            } else {
                                RedFlagVoiceType.SESSION_STOP
                            }
                        onRedFlagVoiceTypeChanged(type)
                        onRedFlagPreviewClicked(type)
                    },
                )
            },
        )
    }
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
            onRedFlagEnabledChanged = {},
            onRedFlagVoiceTypeChanged = {},
            onRedFlagPreviewClicked = {},
            onFlagTextChanged = { _, _ -> },
            onFlagTextPreviewClicked = { _, _ -> },
        )
    }
}
