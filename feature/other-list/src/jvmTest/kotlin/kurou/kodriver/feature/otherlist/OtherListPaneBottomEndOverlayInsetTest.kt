package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.LocalBottomEndOverlayInset
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class OtherListPaneBottomEndOverlayInsetTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `末尾までスクロールすると右下の要素の高さだけ下端を空ける`() {
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalBottomEndOverlayInset provides 100.dp) {
                    Box(Modifier.requiredSize(width = 400.dp, height = 240.dp)) {
                        OtherListPane(
                            uiState =
                                OtherListUiState(
                                    items =
                                        listOf(
                                            OtherListItemType.Voice,
                                            OtherListItemType.Theme,
                                            OtherListItemType.ServerIp,
                                            OtherListItemType.ConsoleIp,
                                            OtherListItemType.Volume,
                                        ),
                                ),
                            onItemClick = {},
                            onOverlayVisibleChange = {},
                            onKeepScreenOnChange = {},
                            onDynamicColorEnabledChange = {},
                            onHapticFeedbackEnabledChange = {},
                            onStartupEnabledChange = {},
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
