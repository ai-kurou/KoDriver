package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualReadoutListPaneEntryAnimationTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `標準表示は初回にだけ移動し登場中の追加も展開する`() {
        assertPaneEntryAnimation(playButtonLayoutEnabled = false)
    }

    @Test
    fun `再生ボタン表示は初回にだけ移動し登場中の追加も展開する`() {
        assertPaneEntryAnimation(playButtonLayoutEnabled = true)
    }

    private fun assertPaneEntryAnimation(playButtonLayoutEnabled: Boolean) {
        val state =
            mutableStateOf(
                ManualReadoutListUiState(
                    playButtonLayoutEnabled = playButtonLayoutEnabled,
                    sets = listOf(ManualReadoutSet("existing", name = "既存セット")),
                ),
            )
        rule.mainClock.autoAdvance = false
        rule.setContent {
            KoDriverTheme {
                Box(Modifier.requiredSize(360.dp, 640.dp)) {
                    ManualReadoutListPaneContent(
                        uiState = state.value,
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        onAddSet = {
                            state.value =
                                state.value.copy(
                                    sets = state.value.sets + ManualReadoutSet("added", name = "追加セット"),
                                )
                        },
                        onReadSet = {},
                        showHotkeyChip = false,
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(32)
        val enteringBounds = rule.onNodeWithText("既存セット").fetchSemanticsNode().boundsInRoot
        rule.onNodeWithText("セットを追加", useUnmergedTree = true).performClick()
        rule.mainClock.advanceTimeBy(64)
        val addedHeight =
            rule
                .onNodeWithText("追加セット")
                .fetchSemanticsNode()
                .boundsInRoot.height
        rule.mainClock.advanceTimeBy(160)
        val settledBounds = rule.onNodeWithText("既存セット").fetchSemanticsNode().boundsInRoot
        assertTrue(enteringBounds.top > settledBounds.top)
        assertTrue(enteringBounds.top - settledBounds.top <= with(rule.density) { 12.dp.toPx() })
        assertEquals(enteringBounds.height, settledBounds.height)
        val settledAddedHeight =
            rule
                .onNodeWithText("追加セット")
                .fetchSemanticsNode()
                .boundsInRoot.height
        assertTrue(addedHeight < settledAddedHeight)
        rule.mainClock.advanceTimeBy(240)
        assertEquals(settledBounds, rule.onNodeWithText("既存セット").fetchSemanticsNode().boundsInRoot)

        rule.runOnIdle {
            state.value = state.value.copy(canReadSets = !state.value.canReadSets)
        }
        rule.mainClock.advanceTimeBy(64)
        assertEquals(settledBounds, rule.onNodeWithText("既存セット").fetchSemanticsNode().boundsInRoot)
        rule.onNodeWithText("追加セット").assertIsDisplayed()
    }
}
