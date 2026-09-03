@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherConsoleIpDetailPaneContentTest {
    @get:Rule
    val rule = createComposeRule()

    private data class ContentParams(
        val uiState: OtherConsoleIpDetailUiState =
            OtherConsoleIpDetailUiState(
                inputAddress = "192.168.1.1",
                isInputValid = true,
            ),
        val onPortSelected: (Int) -> Unit = {},
        val onSave: () -> Unit = {},
        val onDismiss: () -> Unit = {},
        val onBack: () -> Unit = {},
        val onOpenGuide: () -> Unit = {},
        val onOpenSimHub: () -> Unit = {},
    )

    private fun setContent(params: ContentParams = ContentParams()) {
        rule.setContent {
            OtherConsoleIpDetailPaneContent(
                uiState = params.uiState,
                onPortSelected = params.onPortSelected,
                onSave = params.onSave,
                onDismiss = params.onDismiss,
                onBack = params.onBack,
                onOpenGuide = params.onOpenGuide,
                onOpenSimHub = params.onOpenSimHub,
            )
        }
    }

    @Test
    fun `保存ボタンをクリックするとonSaveが呼ばれる`() {
        var saveCount = 0
        setContent(ContentParams(onSave = { saveCount++ }))

        rule.onNodeWithText("保存").performClick()

        assertEquals(1, saveCount)
    }

    @Test
    fun `isSavedがtrueになるとonDismissとonBackが呼ばれる`() {
        var dismissCount = 0
        var backCount = 0
        setContent(
            ContentParams(
                uiState = OtherConsoleIpDetailUiState(isSaved = true),
                onDismiss = { dismissCount++ },
                onBack = { backCount++ },
            ),
        )

        rule.waitForIdle()

        assertEquals(1, dismissCount)
        assertEquals(1, backCount)
    }

    @Test
    fun `戻るボタンをクリックするとonDismissとonBackが呼ばれる`() {
        var dismissCount = 0
        var backCount = 0
        setContent(
            ContentParams(
                onDismiss = { dismissCount++ },
                onBack = { backCount++ },
            ),
        )

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, dismissCount)
        assertEquals(1, backCount)
    }

    @Test
    fun `接続設定ガイドリンクをクリックするとonOpenGuideが呼ばれる`() {
        var guideCount = 0
        setContent(ContentParams(onOpenGuide = { guideCount++ }))

        rule.onNodeWithText("接続設定ガイドを開く").performClick()

        assertEquals(1, guideCount)
    }

    @Test
    fun `SimHubのリンクをタップするとonOpenSimHubが呼ばれる`() {
        var openSimHubCount = 0
        setContent(ContentParams(onOpenSimHub = { openSimHubCount++ }))

        val node = rule.onNodeWithText("ゲーム機または", substring = true)
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        node
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(textLayoutResults)
        val text =
            textLayoutResults
                .first()
                .layoutInput.text.text
        val simHubStart = text.indexOf("SimHub")
        val simHubBoundingBox = textLayoutResults.first().getBoundingBox(simHubStart)
        node.performTouchInput { click(simHubBoundingBox.center) }

        assertEquals(1, openSimHubCount)
    }

    @Test
    fun `33740のラジオボタンをクリックするとonPortSelected(33740)が呼ばれる`() {
        var selected: Int? = null
        setContent(
            ContentParams(
                uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.1", selectedPort = 33741),
                onPortSelected = { selected = it },
            ),
        )

        rule.onNodeWithText("33740（ゲーム機に直接接続）").performClick()

        assertEquals(33740, selected)
    }

    @Test
    fun `33741のラジオボタンをクリックするとonPortSelected(33741)が呼ばれる`() {
        var selected: Int? = null
        setContent(
            ContentParams(
                uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.1", selectedPort = 33740),
                onPortSelected = { selected = it },
            ),
        )

        rule.onNodeWithText("33741（SimHub経由で接続）").performClick()

        assertEquals(33741, selected)
    }

    @Test
    fun `入力欄でEnterキーを押すとonSaveが呼ばれる`() {
        var saveCount = 0
        setContent(ContentParams(onSave = { saveCount++ }))

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(1, saveCount)
    }

    @Test
    fun `保存ボタンが無効な状態で入力欄でEnterキーを押してもonSaveは呼ばれない`() {
        var saveCount = 0
        setContent(
            ContentParams(
                uiState = OtherConsoleIpDetailUiState(inputAddress = "invalid", isInputValid = false),
                onSave = { saveCount++ },
            ),
        )

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(0, saveCount)
    }
}
