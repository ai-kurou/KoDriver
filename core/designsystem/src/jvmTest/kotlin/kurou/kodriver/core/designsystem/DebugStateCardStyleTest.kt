package kurou.kodriver.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugStateCardStyleTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `アクセント色種別は現在のテーマの色に解決される`() {
        val actual = mutableMapOf<DebugStateCardAccent, Color>()
        var expected: Map<DebugStateCardAccent, Color> = emptyMap()
        rule.setContent {
            KoDriverTheme {
                val colors = MaterialTheme.colorScheme
                expected =
                    mapOf(
                        DebugStateCardAccent.PRIMARY to colors.primary,
                        DebugStateCardAccent.ERROR to colors.error,
                        DebugStateCardAccent.WARNING to KoDriverExtendedColors.current.onWarningContainer,
                        DebugStateCardAccent.TERTIARY to colors.tertiary,
                    )
                DebugStateCardAccent.entries.forEach { actual[it] = it.color() }
            }
        }
        rule.waitForIdle()

        assertEquals(expected, actual)
    }

    @Test
    fun `スタイル指定の枠はタイトルと内容を表示し装飾記号は読み上げ対象に含めない`() {
        rule.setContent {
            KoDriverTheme {
                DebugStateCardFrame(
                    title = "タイトル",
                    style = DebugStateCardStyle("🎮", DebugStateCardAccent.PRIMARY),
                ) {
                    Text(text = "内容")
                }
            }
        }

        rule.onNodeWithText("タイトル").assertIsDisplayed()
        rule.onNodeWithText("内容").assertIsDisplayed()
        rule.onNodeWithText("🎮").assertDoesNotExist()
    }
}
