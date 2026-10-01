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
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_description
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_text_selected
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
        onFlagTextPreviewClicked = viewModel::onFlagTextPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutFlagDetailPaneContent(
    uiState: LmuWindowsReadoutFlagDetailUiState,
    onFlagEnabledChanged: (FlagReadoutItem, Boolean) -> Unit,
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
            FlagReadoutCard(
                item = item,
                checked = uiState.enabledStates[item.key] ?: true,
                text = uiState.flagText(item),
                hasReadoutText = uiState.hasReadoutText(item),
                isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                onCheckedChange = { enabled -> onFlagEnabledChanged(item, enabled) },
                onTextChanged = { text -> onFlagTextChanged(item, text) },
                onTextPreviewClick = { text -> onFlagTextPreviewClicked(item, text) },
            )
        }
    }
}

/** 自由文字列の入力と試聴を提供するフラッグ項目のカード。 */
@Suppress("LongParameterList")
@Composable
private fun FlagReadoutCard(
    item: FlagReadoutItem,
    checked: Boolean,
    text: String,
    hasReadoutText: Boolean,
    isTextToSpeechAvailable: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTextChanged: (String) -> Unit,
    onTextPreviewClick: (String) -> Unit,
) {
    DetailPaneCard(
        title = stringResource(item.labelRes),
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        bottomContent = {
            DetailPaneCardTextField(
                value = text,
                placeholder = stringResource(item.labelRes),
                maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                onValueChangeFinished = onTextChanged,
                onPreviewClick = onTextPreviewClick,
                enabled = isTextToSpeechAvailable,
                selected = hasReadoutText,
                supportingText =
                    when {
                        !isTextToSpeechAvailable -> stringResource(Res.string.flag_text_unavailable)
                        hasReadoutText -> stringResource(Res.string.flag_text_selected)
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
            onFlagTextPreviewClicked = { _, _ -> },
        )
    }
}
