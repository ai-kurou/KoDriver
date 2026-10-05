package kurou.kodriver.feature.acewindowsreadout.flagdetail

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
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_description
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_text_preview
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_text_reset_to_default
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_text_selected_icon
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_text_supporting
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_text_unavailable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * AceWindowsReadoutFlagDetail の画面を表示する Composable。
 */
@Composable
fun AceWindowsReadoutFlagDetailPane(modifier: Modifier = Modifier) {
    val viewModel: AceWindowsReadoutFlagDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AceWindowsReadoutFlagDetailPaneContent(
        uiState = uiState,
        onFlagEnabledChanged = viewModel::onFlagEnabledChanged,
        onFlagTextChanged = viewModel::onFlagTextChanged,
        onFlagTextReset = viewModel::onFlagTextReset,
        onFlagTextPreviewClicked = viewModel::onFlagTextPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun AceWindowsReadoutFlagDetailPaneContent(
    uiState: AceWindowsReadoutFlagDetailUiState = AceWindowsReadoutFlagDetailUiState(),
    onFlagEnabledChanged: (FlagReadoutItem, Boolean) -> Unit = { _, _ -> },
    onFlagTextChanged: (FlagReadoutItem, String) -> Unit = { _, _ -> },
    onFlagTextReset: (FlagReadoutItem) -> Unit = {},
    onFlagTextPreviewClicked: (String) -> Unit = {},
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
            val chipLabel = stringResource(item.labelRes)
            val checked = uiState.enabledStates[item.key] ?: true
            DetailPaneCard(
                title = chipLabel,
                checked = checked,
                onCheckedChange = { enabled -> onFlagEnabledChanged(item, enabled) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
                bottomContent = {
                    DetailPaneCardTextField(
                        value = uiState.flagText(item),
                        defaultValue = item.defaultText,
                        onResetToDefault = { onFlagTextReset(item) },
                        resetContentDescription = stringResource(Res.string.flag_text_reset_to_default),
                        placeholder = stringResource(item.labelRes),
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        onValueChangeFinished = { onFlagTextChanged(item, it) },
                        onPreviewClick = onFlagTextPreviewClicked,
                        enabled = uiState.isTextToSpeechAvailable,
                        selected = uiState.hasReadoutText(item),
                        supportingText =
                            when {
                                !uiState.isTextToSpeechAvailable -> stringResource(Res.string.flag_text_unavailable)
                                uiState.hasReadoutText(item) -> null
                                else -> stringResource(Res.string.flag_text_supporting)
                            },
                        previewContentDescription = stringResource(Res.string.flag_text_preview),
                        selectedContentDescription = stringResource(Res.string.flag_text_selected_icon),
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutFlagDetailPanePreview() {
    KoDriverTheme {
        AceWindowsReadoutFlagDetailPaneContent(
            uiState =
                AceWindowsReadoutFlagDetailUiState(
                    enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                    isTextToSpeechAvailable = true,
                ),
        )
    }
}
