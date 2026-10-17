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
class DebugStateCurrentLapCardTest(
    private val value: Gt7Ps5TelemetryData?,
    private val expectedTexts: List<String>,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GT7の値または未取得表示を表示する`() {
        rule.setContent {
            MaterialTheme {
                CurrentLapContent(value)
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
                arrayOf(sampleGt7Ps5Telemetry(), listOf("3")),
                arrayOf(sampleGt7Ps5Telemetry(lapCount = 0), listOf("0")),
            )
    }
}
