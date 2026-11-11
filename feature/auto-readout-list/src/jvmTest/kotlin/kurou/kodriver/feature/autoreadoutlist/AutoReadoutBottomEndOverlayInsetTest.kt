package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.LocalBottomEndOverlayInset
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutoReadoutBottomEndOverlayInsetTest {
    init {
        registerAllReadoutItemKeys()
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `詳細ペインの内容は右下の要素の高さだけ下端を空ける`() {
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalBottomEndOverlayInset provides 52.dp) {
                    AutoReadoutDetailPane(title = "タイトル", canNavigateBack = false, onBack = {}) {
                        Box(Modifier.fillMaxSize().testTag("content"))
                    }
                }
            }
        }

        val gap = rule.onRoot().getBoundsInRoot().bottom - rule.onNodeWithTag("content").getBoundsInRoot().bottom
        assertEquals(52.dp, gap)
    }

    @Test
    fun `一覧は末尾までスクロールすると右下の要素の高さだけ下端を空ける`() {
        val items =
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                LmuWindowsReadoutItemKey.RemainingFuel.Root,
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
            )
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalBottomEndOverlayInset provides 100.dp) {
                    Box(Modifier.requiredSize(width = 400.dp, height = 240.dp)) {
                        AutoReadoutListPane(
                            uiState =
                                AutoReadoutListUiState(
                                    selectedSimulator = Simulator.LmuWindows,
                                    items = items,
                                    autoReadoutEnabledStates = items.associateWith { true },
                                ),
                            onMove = { _, _ -> },
                            onReadoutEnabledChanged = { _, _ -> },
                            onQueueEnabledChanged = { _, _ -> },
                            onStartSoundEnabledChanged = { _, _ -> },
                            onItemClick = {},
                        )
                    }
                }
            }
        }

        repeat(SWIPE_COUNT) { rule.onNode(hasScrollAction()).performTouchInput { swipeUp() } }

        val lastItemBottom =
            rule
                .onNode(hasScrollAction())
                .onChildren()
                .onLast()
                .getBoundsInRoot()
                .bottom
        assertTrue(lastItemBottom <= rule.onRoot().getBoundsInRoot().bottom - 100.dp)
    }

    private companion object {
        const val SWIPE_COUNT = 5
    }
}
