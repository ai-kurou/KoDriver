package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetailPaneScaffoldTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと内容を表示する`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneScaffold(
                    title = "ライセンス",
                    canNavigateBack = true,
                    navigateBackContentDescription = "戻る",
                    onBack = {},
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNodeWithText("ライセンス").assertIsDisplayed()
        rule.onNodeWithText("詳細内容").assertIsDisplayed()
    }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCount = 0
        rule.setContent {
            KoDriverTheme {
                DetailPaneScaffold(
                    title = "ライセンス",
                    canNavigateBack = true,
                    navigateBackContentDescription = "戻る",
                    onBack = { backCount++ },
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `下スクロールでAppBarが折りたたまれ上スクロールで再表示される`() {
        lateinit var scrollBehavior: TopAppBarScrollBehavior
        rule.setContent {
            KoDriverTheme {
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                DetailPaneScaffold(
                    title = "ライセンス",
                    canNavigateBack = true,
                    navigateBackContentDescription = "戻る",
                    onBack = {},
                    scrollBehavior = scrollBehavior,
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        repeat(HEAVY_ITEM_COUNT) { index -> Text("項目$index") }
                    }
                }
            }
        }

        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.waitForIdle()
        assertTrue(scrollBehavior.state.heightOffset < 0f)

        rule.onNode(hasScrollAction()).performTouchInput { swipeDown() }
        rule.waitForIdle()
        assertEquals(0f, scrollBehavior.state.heightOffset)
    }

    @Test
    fun `ホスト幅が840dpを超える場合は内容の幅が840dpに制限される`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(WIDE_HOST_WIDTH, HOST_HEIGHT)) {
                    DetailPaneScaffold(
                        title = "ライセンス",
                        canNavigateBack = true,
                        navigateBackContentDescription = "戻る",
                        onBack = {},
                    ) {
                        Box(modifier = Modifier.fillMaxSize().testTag(CONTENT_TAG))
                    }
                }
            }
        }

        rule.onNodeWithTag(CONTENT_TAG).assertWidthIsEqualTo(DETAIL_PANE_CONTENT_MAX_WIDTH_FOR_TEST)
    }

    @Test
    fun `ホスト幅が840dp以下の場合は内容の幅がホスト幅のまま`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(NARROW_HOST_WIDTH, HOST_HEIGHT)) {
                    DetailPaneScaffold(
                        title = "ライセンス",
                        canNavigateBack = true,
                        navigateBackContentDescription = "戻る",
                        onBack = {},
                    ) {
                        Box(modifier = Modifier.fillMaxSize().testTag(CONTENT_TAG))
                    }
                }
            }
        }

        rule.onNodeWithTag(CONTENT_TAG).assertWidthIsEqualTo(NARROW_HOST_WIDTH)
    }

    private companion object {
        const val HEAVY_ITEM_COUNT = 50
        val WIDE_HOST_WIDTH = 1560.dp
        val NARROW_HOST_WIDTH = 600.dp
        val HOST_HEIGHT = 1080.dp
        val DETAIL_PANE_CONTENT_MAX_WIDTH_FOR_TEST = 840.dp
        const val CONTENT_TAG = "detail_pane_content"
    }
}
