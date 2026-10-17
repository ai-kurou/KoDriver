package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DebugStateFuelConsumptionCardTest(
    private val value: Gt7Ps5TelemetryData?,
    private val expectedTexts: List<String>,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GT7の値または未取得表示を表示する`() {
        rule.setContent {
            MaterialTheme {
                FuelConsumptionContent(value)
            }
        }
        expectedTexts.forEach { rule.onNodeWithText(it).assertIsDisplayed() }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun cases(): List<Array<Any?>> =
            listOf(
                arrayOf(null, listOf("未取得")),
                arrayOf(sampleGt7Ps5Telemetry(gasCapacity = 0f), listOf("未取得")),
                arrayOf(sampleGt7Ps5Telemetry(), listOf("残量 57.1%", "消費 10.0L/周", "残り 4.0周")),
                arrayOf(sampleGt7Ps5Telemetry(lapCount = 0, gasLevel = 35f), listOf("残量 50.0%")),
            )
    }
}
