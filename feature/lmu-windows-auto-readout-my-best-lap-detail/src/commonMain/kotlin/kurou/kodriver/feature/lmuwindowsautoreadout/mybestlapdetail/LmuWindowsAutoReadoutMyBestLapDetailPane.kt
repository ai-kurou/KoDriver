package kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.PlaceholderReadoutTextField
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsMyBestLapReadoutPlaceholders
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_description
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_enabled
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_laptime_insert
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_laptime_placeholder_hint
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_text_label
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_text_preview
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_text_selected_icon
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_text_supporting
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_text_unavailable
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.generated.resources.my_best_lap_text_unknown_placeholders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * LmuWindowsAutoReadoutMyBestLapDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsAutoReadoutMyBestLapDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsAutoReadoutMyBestLapDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    LmuWindowsAutoReadoutMyBestLapDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsAutoReadoutMyBestLapDetailPaneContent(
    uiState: LmuWindowsAutoReadoutMyBestLapDetailUiState = LmuWindowsAutoReadoutMyBestLapDetailUiState(),
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
                PlaceholderReadoutTextField(
                    label = stringResource(Res.string.my_best_lap_text_label),
                    savedText = uiState.readoutText,
                    defaultText = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
                    maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                    isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                    onTextChanged = onReadoutTextChanged,
                    onPreviewClick = onReadoutTextPreviewClicked,
                    placeholder = LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
                    insertLabel = stringResource(Res.string.my_best_lap_laptime_insert),
                    placeholderHint = stringResource(Res.string.my_best_lap_laptime_placeholder_hint),
                    resetContentDescription = stringResource(Res.string.my_best_lap_text_reset_to_default),
                    previewContentDescription = stringResource(Res.string.my_best_lap_text_preview),
                    selectedContentDescription = stringResource(Res.string.my_best_lap_text_selected_icon),
                    unavailableText = stringResource(Res.string.my_best_lap_text_unavailable),
                    supportingText = stringResource(Res.string.my_best_lap_text_supporting),
                    modifier = Modifier.fillMaxWidth(),
                    unknownPlaceholdersText = { currentText ->
                        findUnknownLmuWindowsMyBestLapReadoutPlaceholders(currentText)
                            .joinToString("、")
                            .takeIf { it.isNotEmpty() }
                            ?.let { stringResource(Res.string.my_best_lap_text_unknown_placeholders, it) }
                    },
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsAutoReadoutMyBestLapDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsAutoReadoutMyBestLapDetailPaneContent()
    }
}
