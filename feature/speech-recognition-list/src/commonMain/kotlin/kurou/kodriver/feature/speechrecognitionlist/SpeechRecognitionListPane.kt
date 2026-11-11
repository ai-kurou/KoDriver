package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverPhraseChip
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
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_phrase_count
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
private val STRIPE_WIDTH = 5.dp
private val CATEGORY_ICON_SIZE = 18.dp
private val STEP_NUMBER_SIZE = 20.dp
private const val FLOW_LINE_ALPHA = 0.35f

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
                top = KoDriverSpacing.large,
                bottom = BOTTOM_END_OVERLAY_CLEARANCE + KoDriverSpacing.large + bottomEndOverlayInset,
            ),
        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        item { WakeWordGuide() }
        if (uiState.loaded && uiState.groups.isEmpty()) {
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
                CategoryHeader(group.category, group.items.size)
            }
            items(group.items, key = { "item-${it.key}" }) { SpeechCommandGuideCard(group.category, it) }
        }
    }
}

/** 一覧の先頭に置く、呼びかけの案内。「コドライバー」を大きく見せ、「確認音 → 語句を言う → 読み上げ」の流れを添える。 */
@Composable
private fun WakeWordGuide() {
    val stripeColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .drawBehind { drawRect(stripeColor, size = size.copy(width = STRIPE_WIDTH.toPx())) }
                    .padding(
                        start = KoDriverSpacing.large + KoDriverSpacing.extraSmall,
                        top = KoDriverSpacing.medium,
                        end = KoDriverSpacing.large,
                        bottom = KoDriverSpacing.medium,
                    ),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.speech_command_guide_wake_title),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = stringResource(Res.string.speech_command_guide_wake_word),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
            ) {
                val steps =
                    listOf(
                        stringResource(Res.string.speech_command_guide_wake_step_chime),
                        stringResource(Res.string.speech_command_guide_wake_step_phrase),
                        stringResource(Res.string.speech_command_guide_wake_step_readout),
                    )
                steps.forEachIndexed { index, step ->
                    if (index != 0) {
                        // 流れを示すだけの装飾なので、読み上げの対象から外す
                        HorizontalDivider(
                            modifier = Modifier.weight(1f).clearAndSetSemantics {},
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = FLOW_LINE_ALPHA),
                        )
                    }
                    WakeStep(number = index + 1, label = step)
                }
            }
        }
    }
}

@Composable
private fun WakeStep(
    number: Int,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
    ) {
        Surface(
            modifier = Modifier.size(STEP_NUMBER_SIZE),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number.toString(), style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

/** 読み上げ項目の種類ごとの見出し。種類の色のアイコンと名前、罫線、項目数を並べる。 */
@Composable
private fun CategoryHeader(
    category: ManualReadoutItemCategory,
    itemCount: Int,
) {
    val color = speechCommandGuideCategoryColor(category)
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = KoDriverSpacing.extraSmall, top = KoDriverSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        Icon(
            imageVector = speechCommandGuideCategoryIcon(category),
            contentDescription = null,
            modifier = Modifier.size(CATEGORY_ICON_SIZE),
            tint = color,
        )
        Text(
            text = speechCommandGuideCategoryLabel(category),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = itemCount.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 読み上げ項目1つ分のカード。種類の色の帯を左に置き、項目名と、その項目を読み上げさせる語句をすべて並べる。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeechCommandGuideCard(
    category: ManualReadoutItemCategory,
    item: SpeechCommandGuideItem,
) {
    val color = speechCommandGuideCategoryColor(category)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier =
                Modifier
                    .drawBehind { drawRect(color, size = size.copy(width = STRIPE_WIDTH.toPx())) }
                    .padding(
                        start = KoDriverSpacing.large + KoDriverSpacing.extraSmall,
                        top = KoDriverSpacing.medium,
                        end = KoDriverSpacing.large,
                        bottom = KoDriverSpacing.medium,
                    ),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                Icon(
                    imageVector = speechCommandGuideCategoryIcon(category),
                    contentDescription = null,
                    modifier = Modifier.size(CATEGORY_ICON_SIZE),
                    tint = color,
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.speech_command_guide_phrase_count, item.phrases.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                item.phrases.forEach { KoDriverPhraseChip(it, accent = color) }
            }
        }
    }
}

/** 読み上げカテゴリ専用の拡張カラー。手動タブと同じ色にそろえる。 */
@Composable
private fun speechCommandGuideCategoryColor(category: ManualReadoutItemCategory): Color {
    val colors = KoDriverExtendedColors.current
    return when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> colors.categoryVirtualEnergy
        ManualReadoutItemCategory.FUEL -> colors.categoryFuel
        ManualReadoutItemCategory.TYRE -> colors.categoryTyre
        ManualReadoutItemCategory.BRAKE -> colors.categoryBrake
        ManualReadoutItemCategory.LAP -> colors.categoryLap
    }
}

/** 手動タブの種類チップと同じアイコン。 */
private fun speechCommandGuideCategoryIcon(category: ManualReadoutItemCategory): ImageVector =
    when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> Icons.Default.Bolt
        ManualReadoutItemCategory.FUEL -> Icons.Default.LocalGasStation
        ManualReadoutItemCategory.TYRE -> Icons.Default.DonutLarge
        ManualReadoutItemCategory.BRAKE -> Icons.Default.Speed
        ManualReadoutItemCategory.LAP -> Icons.Default.Timer
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
                                        SpeechCommandGuideItem("fuel", "燃料残量", listOf("燃料の残量", "ガス", "残り燃料")),
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
