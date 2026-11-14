package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kurou.kodriver.core.designsystem.ScrollToTopEffect
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.Res
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_brake
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_fuel
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_lap
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_tyre
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_category_virtual_energy
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_longest_phrase_note
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_phrase_count
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_step_chime
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_step_phrase
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_step_readout
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_title
import kurou.kodriver.feature.speechrecognitionlist.generated.resources.speech_command_guide_wake_word
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

internal const val SPEECH_RECOGNITION_LIST_PANE_TEST_TAG = "speech_recognition_list_pane"

private val BOTTOM_END_OVERLAY_CLEARANCE = 56.dp
private val STRIPE_WIDTH = 5.dp
private val CATEGORY_ICON_SIZE = 18.dp
private val STEP_NUMBER_SIZE = 20.dp
private const val FLOW_LINE_ALPHA = 0.35f
private const val BLINK_COUNT = 3
private const val BLINK_HALF_PERIOD_MS = 300

/**
 * 「音声」タブの一覧（listPane）。選択中のシミュレーターについて、「コドライバー」と呼びかけたあとに言うと読み上げられる語句を、
 * 読み上げ項目ごとのカードで一覧にする。[scrollToTopRequest] が増えるたびに先頭へスクロールする（タブ再タップ用）。[recognitionCard] には、呼びかけの案内の下に置く音声認識の状態カードを渡す
 * （feature 同士は依存しないため、呼び出し側が差し込む）。
 */
@Composable
fun SpeechRecognitionListPane(
    modifier: Modifier = Modifier,
    scrollToTopRequest: Int = 0,
    recognitionCard: @Composable () -> Unit = {},
) {
    val viewModel: SpeechRecognitionListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SpeechRecognitionListPaneContent(
        uiState = uiState,
        modifier = modifier,
        scrollToTopRequest = scrollToTopRequest,
        recognitionCard = recognitionCard,
    )
}

@Composable
internal fun SpeechRecognitionListPaneContent(
    uiState: SpeechRecognitionListUiState,
    modifier: Modifier = Modifier,
    scrollToTopRequest: Int = 0,
    recognitionCard: @Composable () -> Unit = {},
) {
    val bottomEndOverlayInset = LocalBottomEndOverlayInset.current
    val listState = rememberLazyListState()
    val highlight = uiState.highlight
    LaunchedEffect(highlight) {
        if (highlight != null) listState.scrollToSpeechCommandItem(uiState.groups, highlight.itemKey)
    }
    ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
        listState.animateScrollToItem(0)
    }
    LazyColumn(
        state = listState,
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
        item { recognitionCard() }
        uiState.groups.forEach { group ->
            item(key = "category-${group.category.name}") {
                CategoryHeader(group.category, group.items.size)
            }
            items(group.items, key = { speechCommandItemListKey(it.key) }) { item ->
                SpeechCommandGuideCard(
                    category = group.category,
                    item = item,
                    highlight = highlight?.takeIf { it.itemKey == item.key },
                )
            }
        }
    }
}

private fun speechCommandItemListKey(itemKey: String) = "item-$itemKey"

/**
 * 読み上げられた項目のカードが見える位置までスクロールする。既に全体が見えているときは動かさない。
 * 案内にない項目は何もしない。
 */
private suspend fun LazyListState.scrollToSpeechCommandItem(
    groups: List<SpeechCommandGuideGroup>,
    itemKey: String,
) {
    // 先頭は呼びかけの案内と音声認識の状態カード（スロットが空でも item は残る）、続いて種類ごとに「見出し + 項目」が並ぶ
    var index = 2
    for (group in groups) {
        index += 1
        val offset = group.items.indexOfFirst { it.key == itemKey }
        if (offset >= 0) {
            val key = speechCommandItemListKey(itemKey)
            val info = layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }
            val fullyVisible =
                info != null &&
                    info.offset >= layoutInfo.viewportStartOffset &&
                    info.offset + info.size <= layoutInfo.viewportEndOffset
            if (!fullyVisible) animateScrollToItem(index + offset)
            return
        }
        index += group.items.size
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
            Text(
                text = stringResource(Res.string.speech_command_guide_longest_phrase_note),
                style = MaterialTheme.typography.bodySmall,
            )
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
    highlight: SpeechPhraseHighlight?,
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
                item.phrases.forEach { phrase ->
                    PhraseChip(
                        phrase = phrase,
                        accent = color,
                        highlight = highlight?.takeIf { it.phrase == phrase },
                    )
                }
            }
        }
    }
}

/** 語句のチップ。読み上げの決め手になった語句は、指示のたびに数回点滅させる。 */
@Composable
private fun PhraseChip(
    phrase: String,
    accent: Color,
    highlight: SpeechPhraseHighlight?,
) {
    val emphasis = remember { Animatable(0f) }
    LaunchedEffect(highlight) {
        if (highlight == null) {
            emphasis.snapTo(0f)
        } else {
            repeat(BLINK_COUNT) {
                emphasis.animateTo(1f, tween(BLINK_HALF_PERIOD_MS))
                emphasis.animateTo(0f, tween(BLINK_HALF_PERIOD_MS))
            }
        }
    }
    KoDriverPhraseChip(phrase, accent = accent, emphasis = emphasis.value)
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
