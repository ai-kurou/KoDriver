package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class ManualReadoutItemsCardCategoryBarTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `カテゴリが取れる項目では入力欄の左にカテゴリ名のバーを表示する`() {
        val key = LmuWindowsReadoutItemKey.RemainingFuel.Root.value
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    availableItemKeys = listOf(key),
                    selectedItemKeys = listOf(key),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemsCard(state.value, {}, {}, { _, _ -> }, { _, _ -> }, {})
            }
        }

        rule.onNodeWithContentDescription("燃料").assertExists()
    }

    @Test
    fun `カテゴリが取れない項目ではカテゴリのバーを表示しない`() {
        val key = "unknown_readout_item_key"
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    availableItemKeys = listOf(key),
                    selectedItemKeys = listOf(key),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemsCard(state.value, {}, {}, { _, _ -> }, { _, _ -> }, {})
            }
        }

        rule.onNodeWithContentDescription("燃料").assertDoesNotExist()
    }

    @Test
    fun `カテゴリ色のバーはドラッグハンドルの行から文言欄の下まで通す`() {
        val key = LmuWindowsReadoutItemKey.RemainingFuel.Root.value
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    availableItemKeys = listOf(key),
                    selectedItemKeys = listOf(key),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemsCard(state.value, {}, {}, { _, _ -> }, { _, _ -> }, {})
            }
        }

        val barBounds = rule.onNodeWithContentDescription("燃料").getBoundsInRoot()
        val handleBounds = rule.onNodeWithContentDescription("燃料残量を並べ替え").getBoundsInRoot()
        val textFieldBounds = rule.onNode(hasSetTextAction()).getBoundsInRoot()

        assertTrue(barBounds.top <= handleBounds.top)
        assertTrue(barBounds.bottom >= textFieldBounds.bottom)
        assertTrue(barBounds.bottom - barBounds.top > handleBounds.bottom - handleBounds.top)
    }
}
