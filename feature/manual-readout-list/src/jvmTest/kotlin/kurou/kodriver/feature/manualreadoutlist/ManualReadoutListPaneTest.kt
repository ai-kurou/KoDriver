package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class ManualReadoutListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `選択中のシミュレーター名を含む空状態を表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPane(simulatorName = "LMU")
            }
        }

        rule.onNodeWithText("手動読み上げについて").assertIsDisplayed()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertIsDisplayed()
    }

    @Test
    fun `シミュレーター名を変更すると空状態の文言を更新する`() {
        val simulatorName = mutableStateOf("LMU")
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPane(simulatorName = simulatorName.value)
            }
        }

        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertIsDisplayed()
        rule.runOnIdle { simulatorName.value = "ACE" }
        rule.onNodeWithText("ACEの手動読み上げセットはまだありません").assertIsDisplayed()
        rule.onNodeWithText("LMUの手動読み上げセットはまだありません").assertDoesNotExist()
    }

    @Test
    fun `ヘルプを開くと手動読み上げの説明を表示する`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                    ManualReadoutListPane(simulatorName = "LMU")
                }
            }
        }

        rule.onNodeWithContentDescription("手動読み上げの説明を表示").performClick()

        rule.onNodeWithText("読み上げ").assertIsDisplayed()
        rule.onNodeWithText("順番待ち").assertIsDisplayed()
        rule.onNodeWithText("データがないとき").assertIsDisplayed()
        rule
            .onNodeWithText("自動読み上げのON/OFFに関係なく読み上げます。自動読み上げの「読み上げ済み」の状態は変わりません。")
            .performScrollTo()
            .assertIsDisplayed()
        rule
            .onNodeWithText("読み上げ中のときや自動読み上げと重なったときは、キューの最後に並んで順番に読み上げます。")
            .performScrollTo()
            .assertIsDisplayed()
        rule
            .onNodeWithText("読み上げる値がない項目は「データがありません」と読み上げます。")
            .performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText("ホットキー").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `ホットキーの説明を有効にすると表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutHintSheetContent(showHotkeyHint = true)
            }
        }

        rule.onNodeWithText("ホットキー").assertIsDisplayed()
        rule
            .onNodeWithText("Ctrl + Alt + Shift と数字または英字キーの組み合わせで、セットを読み上げられます。")
            .assertIsDisplayed()
    }

    @Test
    fun `ホットキーの説明を無効にすると表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutHintSheetContent(showHotkeyHint = false)
            }
        }

        rule.onNodeWithText("読み上げ").assertIsDisplayed()
        rule.onNodeWithText("順番待ち").assertIsDisplayed()
        rule.onNodeWithText("データがないとき").assertIsDisplayed()
        rule.onNodeWithText("ホットキー").assertDoesNotExist()
        rule
            .onNodeWithText("Ctrl + Alt + Shift と数字または英字キーの組み合わせで、セットを読み上げられます。")
            .assertDoesNotExist()
    }
}
