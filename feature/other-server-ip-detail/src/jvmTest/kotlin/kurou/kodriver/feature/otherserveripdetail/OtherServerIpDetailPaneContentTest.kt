package kurou.kodriver.feature.otherserveripdetail

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

class OtherServerIpDetailPaneContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherServerIpDetailUiState = OtherServerIpDetailUiState(inputIp = "192.168.1.1", isInputValid = true),
        onSave: () -> Unit = {},
        onSaveAnyway: () -> Unit = {},
        onDismiss: () -> Unit = {},
        onOpenGuide: () -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        rule.setContent {
            OtherServerIpDetailPaneContent(
                uiState = uiState,
                onSave = onSave,
                onSaveAnyway = onSaveAnyway,
                onDismiss = onDismiss,
                onOpenGuide = onOpenGuide,
                onBack = onBack,
            )
        }
    }

    @Test
    fun `保存ボタンをクリックするとonSaveが呼ばれる`() {
        var saveCount = 0
        setContent(onSave = { saveCount++ })

        rule.onNodeWithText("保存").performClick()

        assertEquals(1, saveCount)
    }

    @Test
    fun `このまま保存ボタンをクリックするとonSaveAnywayが呼ばれる`() {
        var saveAnywayCount = 0
        setContent(
            uiState =
                OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    isInputValid = true,
                    connectivityWarning = true,
                ),
            onSaveAnyway = { saveAnywayCount++ },
        )

        rule.onNodeWithText("このまま保存").performClick()

        assertEquals(1, saveAnywayCount)
    }

    @Test
    fun `isSavedがtrueになるとonDismissとonBackが呼ばれる`() {
        var dismissCount = 0
        var backCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(isSaved = true),
            onDismiss = { dismissCount++ },
            onBack = { backCount++ },
        )

        rule.waitForIdle()

        assertEquals(1, dismissCount)
        assertEquals(1, backCount)
    }

    @Test
    fun `インストールガイドリンクをクリックするとonOpenGuideが呼ばれる`() {
        var guideCount = 0
        setContent(onOpenGuide = { guideCount++ })

        rule.onNodeWithText("インストールガイドを開く").performClick()

        assertEquals(1, guideCount)
    }

    @Test
    fun `説明文のWindows版KoDriverリンクをクリックするとonOpenGuideが呼ばれる`() {
        var guideCount = 0
        setContent(onOpenGuide = { guideCount++ })

        val node = rule.onNodeWithText("が動作しているPC", substring = true)
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        node
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(textLayoutResults)
        val textLayoutResult = textLayoutResults.first()
        val linkStart =
            textLayoutResult.layoutInput.text.text
                .indexOf("Windows版KoDriver")
        val boundingBox = textLayoutResult.getBoundingBox(linkStart)
        node.performTouchInput { click(boundingBox.center) }

        assertEquals(1, guideCount)
    }

    @Test
    fun `入力欄でEnterキーを押すとonSaveが呼ばれる`() {
        var saveCount = 0
        setContent(onSave = { saveCount++ })

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(1, saveCount)
    }

    @Test
    fun `接続警告表示中に入力欄でEnterキーを押すとonSaveAnywayが呼ばれる`() {
        var saveAnywayCount = 0
        setContent(
            uiState =
                OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    isInputValid = true,
                    connectivityWarning = true,
                ),
            onSaveAnyway = { saveAnywayCount++ },
        )

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(1, saveAnywayCount)
    }

    @Test
    fun `保存ボタンが無効な状態で入力欄でEnterキーを押してもonSaveは呼ばれない`() {
        var saveCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
            onSave = { saveCount++ },
        )

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(0, saveCount)
    }

    @Test
    fun `戻るボタンをクリックするとonDismissとonBackが呼ばれる`() {
        var dismissCount = 0
        var backCount = 0
        setContent(
            onDismiss = { dismissCount++ },
            onBack = { backCount++ },
        )

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, dismissCount)
        assertEquals(1, backCount)
    }
}
