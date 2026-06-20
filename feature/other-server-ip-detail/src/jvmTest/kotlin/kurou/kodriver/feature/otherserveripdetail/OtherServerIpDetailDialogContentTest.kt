@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherServerIpDetailDialogContentTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherServerIpDetailUiState = OtherServerIpDetailUiState(inputIp = "192.168.1.1", isInputValid = true),
        onSave: () -> Unit = {},
        onSaveAnyway: () -> Unit = {},
        onDismiss: () -> Unit = {},
        onSaved: () -> Unit = {},
    ) {
        rule.setContent {
            OtherServerIpDetailDialogContent(
                uiState = uiState,
                onSave = onSave,
                onSaveAnyway = onSaveAnyway,
                onDismiss = onDismiss,
                onSaved = onSaved,
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
    fun `キャンセルボタンをクリックするとonDismissが呼ばれる`() {
        var dismissCount = 0
        setContent(onDismiss = { dismissCount++ })

        rule.onNodeWithText("キャンセル").performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun `このまま保存ボタンをクリックするとonSaveAnywayが呼ばれる`() {
        var saveAnywayCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(
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
    fun `isSavedがtrueになるとonSavedが呼ばれる`() {
        var savedCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(isSaved = true),
            onSaved = { savedCount++ },
        )

        rule.waitForIdle()

        assertEquals(1, savedCount)
    }
}
