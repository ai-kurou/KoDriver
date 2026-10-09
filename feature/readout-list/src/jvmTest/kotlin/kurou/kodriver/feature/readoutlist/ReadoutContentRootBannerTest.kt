package kurou.kodriver.feature.readoutlist

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ReadoutContentRootBannerTest {
    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.compute(400f, 800f)

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
    fun `選択中項目のRootがOFFの場合は詳細ペインにバナーを表示しONにするで有効化コールバックを呼ぶ`() {
        var tyreTemperatureText by mutableStateOf("")
        val changes = mutableListOf<Pair<ReadoutItemKey, Boolean>>()

        rule.setContent {
            tyreTemperatureText = itemDisplayName(ReadoutItemKey.LmuWindows.TyreTemperature.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                        readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to false),
                        selectedItem = ReadoutListItemType.LmuWindows.TyreTemperature,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { key, enabled -> changes += key to enabled },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText("「$tyreTemperatureText」がOFFのため、読み上げられません").assertExists()
        rule.onNodeWithText("ONにする").performClick()

        assertEquals(
            listOf<Pair<ReadoutItemKey, Boolean>>(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
            changes,
        )
    }

    @Test
    fun `選択中項目のRootがONの場合は詳細ペインにバナーを表示しない`() {
        rule.setContent {
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                        readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true),
                        selectedItem = ReadoutListItemType.LmuWindows.TyreTemperature,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText("ONにする").assertDoesNotExist()
    }

    @Test
    fun `Root状態が未読込でデフォルトがtrueの項目は詳細ペインにバナーを表示しない`() {
        rule.setContent {
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                        readoutEnabledStates = emptyMap(),
                        selectedItem = ReadoutListItemType.LmuWindows.TyreTemperature,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText("ONにする").assertDoesNotExist()
    }

    @Test
    fun `Root状態が未読込でデフォルトがfalseの項目は詳細ペインにバナーを表示する`() {
        rule.setContent {
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                        readoutEnabledStates = emptyMap(),
                        selectedItem = ReadoutListItemType.LmuWindows.VehicleDamage,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText("ONにする").assertExists()
    }
}
