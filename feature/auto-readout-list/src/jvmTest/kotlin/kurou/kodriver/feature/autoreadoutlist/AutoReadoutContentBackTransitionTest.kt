package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class AutoReadoutContentBackTransitionTest {
    init {
        registerAllReadoutItemKeys()
    }

    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(400f, 800f)

    private val singlePaneDirective =
        PaneScaffoldDirective(
            maxHorizontalPartitions = 1,
            horizontalPartitionSpacerSize = 0.dp,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = 360.dp,
            excludedBounds = emptyList(),
        )

    @Test
    fun `LMUの自己ベストラップは詳細を閉じた直後も退場アニメーション中は詳細を描画する`() {
        assertDetailAfterClear(
            item = LmuWindowsAutoReadoutListItemType.MyBestLap,
            key = LmuWindowsReadoutItemKey.MyBestLap.Root,
            expectedDetailCountDuringExit = 1,
        )
    }

    @Test
    fun `LMUの自己ベストラップ以外の項目も詳細を閉じた直後は退場アニメーション中は詳細を描画する`() {
        assertDetailAfterClear(
            item = LmuWindowsAutoReadoutListItemType.TyreWear,
            key = LmuWindowsReadoutItemKey.TyreWear.Root,
            expectedDetailCountDuringExit = 1,
        )
    }

    @Test
    fun `GT7の項目も詳細を閉じた直後は退場アニメーション中は詳細を描画する`() {
        assertDetailAfterClear(
            item = Gt7Ps5AutoReadoutListItemType.MyBestLap,
            key = Gt7Ps5ReadoutItemKey.MyBestLap.Root,
            expectedDetailCountDuringExit = 1,
            simulator = Simulator.Gt7Ps5,
        )
    }

    @Test
    fun `ACEの項目は詳細を閉じると直ちに詳細を描画しない`() {
        assertDetailAfterClear(
            item = AceWindowsAutoReadoutListItemType.Flag,
            key = AceWindowsReadoutItemKey.Flag.Root,
            expectedDetailCountDuringExit = 0,
            simulator = Simulator.AceWindows,
        )
    }

    private fun assertDetailAfterClear(
        item: AutoReadoutListItemType,
        key: ReadoutItemKey,
        expectedDetailCountDuringExit: Int,
        simulator: Simulator = Simulator.LmuWindows,
    ) {
        var selectedItem by mutableStateOf<AutoReadoutListItemType?>(item)
        rule.mainClock.autoAdvance = false

        rule.setContent {
            AutoReadoutContent(
                uiState =
                    AutoReadoutListUiState(
                        selectedSimulator = simulator,
                        items = listOf(key),
                        autoReadoutEnabledStates = mapOf(key to true),
                        selectedItem = selectedItem,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                detailContent = { Text(DETAIL_CONTENT_TEXT) },
            )
        }
        rule.mainClock.advanceTimeBy(1_000)
        rule.onAllNodesWithText(DETAIL_CONTENT_TEXT).assertCountEquals(1)

        rule.runOnIdle { selectedItem = null }
        rule.mainClock.advanceTimeByFrame()

        rule.onAllNodesWithText(DETAIL_CONTENT_TEXT).assertCountEquals(expectedDetailCountDuringExit)

        rule.mainClock.advanceTimeBy(1_000)

        rule.onAllNodesWithText(DETAIL_CONTENT_TEXT).assertCountEquals(0)
    }

    private companion object {
        const val DETAIL_CONTENT_TEXT = "詳細の内容"
    }
}
