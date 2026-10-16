package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.readout_item_tyre_temperature
import kurou.kodriver.core.designsystem.generated.resources.readout_item_vehicle_approach
import org.junit.Rule
import org.junit.Test

class DebugStateCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `有効なカードのタイトルと状態に対応する内容を表示してキーとmodifierをフレームに渡す`() {
        assertCard(enabled = true, cardKey = "vehicle", title = "車両接近", content = "車両 状態")
    }

    @Test
    fun `無効なカードもタイトルと状態に対応する内容を表示してキーとmodifierをフレームに渡す`() {
        assertCard(enabled = false, cardKey = "tyre", title = "タイヤ温度", content = "タイヤ 状態")
    }

    private fun assertCard(
        enabled: Boolean,
        cardKey: String,
        title: String,
        content: String,
    ) {
        val contents: Map<String, @Composable (String) -> Unit> =
            mapOf(
                "vehicle" to { state -> Text("車両 $state") },
                "tyre" to { state -> Text("タイヤ $state") },
            )
        val titles =
            mapOf(
                "vehicle" to Res.string.readout_item_vehicle_approach,
                "tyre" to Res.string.readout_item_tyre_temperature,
            )
        rule.setContent {
            KoDriverTheme {
                DebugStateCard(
                    cardKey = cardKey,
                    uiState = "状態",
                    enabled = enabled,
                    titleOf = titles::getValue,
                    contentOf = contents::getValue,
                    frame = { key, resolvedTitle, modifier, cardContent ->
                        Column(modifier) {
                            Text(key)
                            Text(resolvedTitle)
                            cardContent()
                        }
                    },
                    modifier = Modifier.testTag("card"),
                )
            }
        }

        rule.onNodeWithText(cardKey).assertIsDisplayed()
        rule.onNodeWithText(title).assertIsDisplayed()
        rule.onNodeWithText(content).assertIsDisplayed()
        rule.onNodeWithTag("card").assertIsDisplayed()
    }
}
