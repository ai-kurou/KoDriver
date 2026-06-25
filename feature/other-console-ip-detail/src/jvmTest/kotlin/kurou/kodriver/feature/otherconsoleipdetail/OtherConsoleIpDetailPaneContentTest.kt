@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherConsoleIpDetailPaneContentTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherConsoleIpDetailUiState = OtherConsoleIpDetailUiState(
            inputAddress = "192.168.1.1",
            isInputValid = true,
        ),
        onSave: () -> Unit = {},
        onDismiss: () -> Unit = {},
        onBack: () -> Unit = {},
        onOpenGuide: () -> Unit = {},
    ) {
        rule.setContent {
            OtherConsoleIpDetailPaneContent(
                uiState = uiState,
                onSave = onSave,
                onDismiss = onDismiss,
                onBack = onBack,
                onOpenGuide = onOpenGuide,
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
    fun `isSavedがtrueになるとonDismissとonBackが呼ばれる`() {
        var dismissCount = 0
        var backCount = 0
        setContent(
            uiState = OtherConsoleIpDetailUiState(isSaved = true),
            onDismiss = { dismissCount++ },
            onBack = { backCount++ },
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
            onDismiss = { dismissCount++ },
            onBack = { backCount++ },
        )

        rule.onNodeWithTag("other_detail_back").performClick()

        assertEquals(1, dismissCount)
        assertEquals(1, backCount)
    }

    @Test
    fun `接続設定ガイドリンクをクリックするとonOpenGuideが呼ばれる`() {
        var guideCount = 0
        setContent(onOpenGuide = { guideCount++ })

        rule.onNodeWithText("接続設定ガイドを開く").performClick()

        assertEquals(1, guideCount)
    }
}
