package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.LocalBottomEndOverlayInset
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.Res
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_brake
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_fuel
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_lap
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_tyre
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_virtual_energy
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_empty
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_step_chime
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_step_phrase
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_step_readout
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_title
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_word
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

internal const val SPEECH_RECOGNITION_LIST_PANE_TEST_TAG = "speech_recognition_list_pane"
internal const val SPEECH_RECOGNITION_LIST_EMPTY_TEST_TAG = "speech_recognition_list_empty"

private val BOTTOM_END_OVERLAY_CLEARANCE = 56.dp

/**
 * 「音声」タブの一覧（listPane）。選択中のシミュレーターについて、「コドライバー」と呼びかけたあとに言うと読み上げられる語句を、
 * 読み上げ項目ごとのカードで一覧にする。
 */
@Composable
fun SpeechRecognitionListPane(modifier: Modifier = Modifier) {
    val viewModel: SpeechRecognitionListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SpeechRecognitionListPaneContent(uiState = uiState, modifier = modifier)
}

@Composable
internal fun SpeechRecognitionListPaneContent(
    uiState: SpeechRecognitionListUiState,
    modifier: Modifier = Modifier,
) {
    val bottomEndOverlayInset = LocalBottomEndOverlayInset.current
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag(SPEECH_RECOGNITION_LIST_PANE_TEST_TAG),
        contentPadding =
            PaddingValues(
                start = KoDriverSpacing.small,
                end = KoDriverSpacing.small,
                bottom = BOTTOM_END_OVERLAY_CLEARANCE + KoDriverSpacing.large + bottomEndOverlayInset,
            ),
        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        item { WakeWordGuide() }
        if (uiState.groups.isEmpty()) {
            item {
                Text(
                    text = stringResource(Res.string.speech_command_guide_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(KoDriverSpacing.small).testTag(SPEECH_RECOGNITION_LIST_EMPTY_TEST_TAG),
                )
            }
        }
        uiState.groups.forEach { group ->
            item(key = "category-${group.category.name}") {
                Text(
                    text = speechCommandGuideCategoryLabel(group.category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = KoDriverSpacing.extraSmall, top = KoDriverSpacing.small),
                )
            }
            items(group.items, key = { "item-${it.key}" }) { SpeechCommandGuideCard(it) }
        }
    }
}

/** 一覧の先頭に置く、呼びかけの案内。「コドライバー → 確認音 → 語句 → 読み上げ」の流れを示す。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WakeWordGuide() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(KoDriverSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Text(
                text = stringResource(Res.string.speech_command_guide_wake_title),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
            ) {
                val steps =
                    listOf(
                        stringResource(Res.string.speech_command_guide_wake_word),
                        stringResource(Res.string.speech_command_guide_wake_step_chime),
                        stringResource(Res.string.speech_command_guide_wake_step_phrase),
                        stringResource(Res.string.speech_command_guide_wake_step_readout),
                    )
                steps.forEachIndexed { index, step ->
                    if (index != 0) Text(text = FLOW_ARROW, style = MaterialTheme.typography.labelMedium)
                    PhraseChip(step)
                }
            }
        }
    }
}

private const val FLOW_ARROW = "→"

/** 読み上げ項目1つ分のカード。項目名と、その項目を読み上げさせる語句をすべて並べる。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeechCommandGuideCard(item: SpeechCommandGuideItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(KoDriverSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
            ) {
                item.phrases.forEach { PhraseChip(it) }
            }
        }
    }
}

@Composable
private fun PhraseChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        )
    }
}

@Composable
private fun speechCommandGuideCategoryLabel(category: ManualReadoutItemCategory): String =
    stringResource(
        when (category) {
            ManualReadoutItemCategory.VIRTUAL_ENERGY -> Res.string.speech_command_guide_category_virtual_energy
            ManualReadoutItemCategory.FUEL -> Res.string.speech_command_guide_category_fuel
            ManualReadoutItemCategory.TYRE -> Res.string.speech_command_guide_category_tyre
            ManualReadoutItemCategory.BRAKE -> Res.string.speech_command_guide_category_brake
            ManualReadoutItemCategory.LAP -> Res.string.speech_command_guide_category_lap
        },
    )

@Preview(showBackground = true)
@Composable
private fun SpeechRecognitionListPanePreview() {
    KoDriverTheme {
        SpeechRecognitionListPaneContent(
            uiState =
                SpeechRecognitionListUiState(
                    groups =
                        listOf(
                            SpeechCommandGuideGroup(
                                category = ManualReadoutItemCategory.FUEL,
                                items =
                                    listOf(
                                        SpeechCommandGuideItem("fuel", "燃料残量", listOf("燃料", "ガス", "フューエル", "残り燃料")),
                                    ),
                            ),
                            SpeechCommandGuideGroup(
                                category = ManualReadoutItemCategory.TYRE,
                                items =
                                    listOf(
                                        SpeechCommandGuideItem("tyre_temp", "タイヤ温度", listOf("タイヤ温度", "タイヤ", "温度")),
                                        SpeechCommandGuideItem("tyre_wear", "タイヤ摩耗", listOf("タイヤ摩耗", "摩耗", "タイヤ残り")),
                                    ),
                            ),
                        ),
                ),
        )
    }
}
