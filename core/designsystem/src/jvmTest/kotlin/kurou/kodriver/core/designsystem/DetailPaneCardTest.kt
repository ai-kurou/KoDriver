package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DetailPaneCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `OFF状態でもタイトルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCard(
                    title = "車両接近",
                    checked = false,
                    onCheckedChange = {},
                    bottomContent = {
                        DetailPaneCardChips(
                            chipLabels = listOf("カーレフト"),
                            selectedChipLabels = emptySet(),
                            chipEnabled = false,
                            onChipClick = {},
                        )
                    },
                )
            }
        }

        rule.onNodeWithText("車両接近").assertIsDisplayed()
    }

    @Test
    fun `chipEnabledがfalseのときチップが操作不可になる`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCard(
                    title = "車両接近",
                    checked = false,
                    onCheckedChange = {},
                    bottomContent = {
                        DetailPaneCardChips(
                            chipLabels = listOf("カーレフト"),
                            selectedChipLabels = emptySet(),
                            chipEnabled = false,
                            onChipClick = {},
                        )
                    },
                )
            }
        }

        rule.onNodeWithText("カーレフト").assertIsNotEnabled()
    }

    @Test
    fun `chipEnabledがtrueのときチップが操作可能になる`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCard(
                    title = "車両接近",
                    checked = true,
                    onCheckedChange = {},
                    bottomContent = {
                        DetailPaneCardChips(
                            chipLabels = listOf("カーレフト"),
                            selectedChipLabels = emptySet(),
                            chipEnabled = true,
                            onChipClick = {},
                        )
                    },
                )
            }
        }

        rule.onNodeWithText("カーレフト").assertIsEnabled()
    }

    @Test
    fun `ヘッダーをタップするとonCheckedChangeが呼ばれる`() {
        var checked: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCard(
                    title = "車両接近",
                    checked = false,
                    onCheckedChange = { checked = it },
                    bottomContent = {},
                )
            }
        }

        rule.onNodeWithText("車両接近").performClick()

        assertEquals(true, checked)
    }

    @Test
    fun `スイッチなしの場合もタイトルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCard(
                    title = "自己ベストラップ更新",
                    bottomContent = {},
                )
            }
        }

        rule.onNodeWithText("自己ベストラップ更新").assertIsDisplayed()
    }
}
