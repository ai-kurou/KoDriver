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

class ManualReadoutSetEntryAnimationTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `標準表示で追加したセットは縦に展開し表示切替で再生しない`() {
        assertSetEntryAnimation(playButtonLayoutEnabled = false)
    }

    @Test
    fun `再生ボタン表示で追加したセットは縦に展開し表示切替で再生しない`() {
        assertSetEntryAnimation(playButtonLayoutEnabled = true)
    }

    @Test
    fun `空の標準表示から最初に追加したセットも縦に展開する`() {
        assertSetEntryAnimation(playButtonLayoutEnabled = false, initiallyEmpty = true)
    }

    private fun assertSetEntryAnimation(
        playButtonLayoutEnabled: Boolean,
        initiallyEmpty: Boolean = false,
    ) {
        val state =
            mutableStateOf(
                ManualReadoutListUiState(
                    playButtonLayoutEnabled = playButtonLayoutEnabled,
                    sets =
                        if (initiallyEmpty) {
                            emptyList()
                        } else {
                            listOf(ManualReadoutSet("existing", name = "既存セット"))
                        },
                ),
            )
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
        if (!initiallyEmpty) {
            rule.onNodeWithText("既存セット").assertIsDisplayed()
        }
        val initialAddLeft =
            rule
                .onNodeWithText("セットを追加", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot.left
        rule.mainClock.autoAdvance = false
        rule.onNodeWithText("セットを追加", useUnmergedTree = true).performClick()
        rule.mainClock.advanceTimeBy(96)
        val enteringHeight =
            rule
                .onNodeWithText("追加セット")
                .fetchSemanticsNode()
                .boundsInRoot.height
        val movingAddLeft =
            rule
                .onNodeWithText("セットを追加", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot.left
        rule.mainClock.advanceTimeBy(400)
        if (playButtonLayoutEnabled) {
            val settledAddLeft =
                rule
                    .onNodeWithText("セットを追加", useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot.left
            assertTrue(movingAddLeft > settledAddLeft)
            assertTrue(movingAddLeft < initialAddLeft)
        }
        val settledHeight =
            rule
                .onNodeWithText("追加セット")
                .fetchSemanticsNode()
                .boundsInRoot.height
        assertTrue(enteringHeight < settledHeight)
        rule.onNodeWithText("追加セット").assertIsDisplayed()

        rule.runOnIdle {
            state.value = state.value.copy(playButtonLayoutEnabled = !playButtonLayoutEnabled)
        }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        val switchedHeight =
            rule
                .onNodeWithText("追加セット")
                .fetchSemanticsNode()
                .boundsInRoot.height
        rule.mainClock.advanceTimeBy(400)
        assertEquals(
            switchedHeight,
            rule
                .onNodeWithText("追加セット")
                .fetchSemanticsNode()
                .boundsInRoot.height,
        )
    }
}
