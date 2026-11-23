package kurou.kodriver.feature.othermicrophonecutquietsounddetail

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
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import kurou.kodriver.feature.othermicrophonecutquietsounddetail.generated.resources.Res
import kurou.kodriver.feature.othermicrophonecutquietsounddetail.generated.resources.cut_quiet_sound_description
import kurou.kodriver.feature.othermicrophonecutquietsounddetail.generated.resources.cut_quiet_sound_label
import kurou.kodriver.feature.othermicrophonecutquietsounddetail.generated.resources.cut_quiet_sound_label_off
import kurou.kodriver.feature.othermicrophonecutquietsounddetail.generated.resources.cut_quiet_sound_reset
import kurou.kodriver.feature.othermicrophonecutquietsounddetail.generated.resources.cut_quiet_sound_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * OtherMicrophoneCutQuietSoundDetail の画面を表示する Composable。
 */
@Composable
public fun OtherMicrophoneCutQuietSoundDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherMicrophoneCutQuietSoundDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherMicrophoneCutQuietSoundDetailPaneContent(
        uiState = uiState,
        onLevelChanged = viewModel::onLevelChanged,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherMicrophoneCutQuietSoundDetail の画面本体を表示する Composable。
 */
@Composable
public fun OtherMicrophoneCutQuietSoundDetailPaneContent(
    uiState: OtherMicrophoneCutQuietSoundDetailUiState,
    onLevelChanged: (Int) -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val levelLabel = stringResource(Res.string.cut_quiet_sound_label)
    val offLabel = stringResource(Res.string.cut_quiet_sound_label_off)

    DetailPaneScaffold(
        title = stringResource(Res.string.cut_quiet_sound_title),
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            DetailPaneDescription(
                text = stringResource(Res.string.cut_quiet_sound_description),
            )
            ThresholdSlider(
                value = uiState.level.toFloat(),
                valueRange =
                    MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN.toFloat()..MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX.toFloat(),
                labelFormatter = { value ->
                    val level = value.roundToInt()
                    if (level == MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN) offLabel else levelLabel.formatSliderLabel(level)
                },
                onValueChangeFinished = { onLevelChanged(it.roundToInt()) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
                steps = MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX - MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN - 1,
                defaultValue = MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT.toFloat(),
                onResetToDefault = { onLevelChanged(MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT) },
                resetContentDescription = stringResource(Res.string.cut_quiet_sound_reset),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherMicrophoneCutQuietSoundDetailPanePreview() {
    KoDriverTheme {
        OtherMicrophoneCutQuietSoundDetailPaneContent(uiState = OtherMicrophoneCutQuietSoundDetailUiState())
    }
}
