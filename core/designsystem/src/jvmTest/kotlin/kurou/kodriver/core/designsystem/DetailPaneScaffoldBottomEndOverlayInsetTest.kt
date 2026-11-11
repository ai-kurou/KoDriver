package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DetailPaneScaffoldBottomEndOverlayInsetTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `右下の要素の高さが提供されていなければ内容は下端まで広がる`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneScaffold(title = "タイトル", canNavigateBack = false, onBack = {}) {
                    Box(Modifier.fillMaxSize().testTag("content"))
                }
            }
        }

        val gap = rule.onRoot().getBoundsInRoot().bottom - rule.onNodeWithTag("content").getBoundsInRoot().bottom
        assertEquals(0.dp, gap)
    }

    @Test
    fun `右下の要素の高さが提供されていれば内容はその分だけ下端を空ける`() {
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalBottomEndOverlayInset provides 52.dp) {
                    DetailPaneScaffold(title = "タイトル", canNavigateBack = false, onBack = {}) {
                        Box(Modifier.fillMaxSize().testTag("content"))
                    }
                }
            }
        }

        val gap = rule.onRoot().getBoundsInRoot().bottom - rule.onNodeWithTag("content").getBoundsInRoot().bottom
        assertEquals(52.dp, gap)
    }
}
