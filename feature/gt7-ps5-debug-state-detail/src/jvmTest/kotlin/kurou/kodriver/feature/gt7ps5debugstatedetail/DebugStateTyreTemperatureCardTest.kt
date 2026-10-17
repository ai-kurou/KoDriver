package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DebugStateTyreTemperatureCardTest(
    private val value: Gt7Ps5TelemetryData?,
    private val expectedTexts: List<String>,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GT7の値または未取得表示を表示する`() {
        rule.setContent {
            MaterialTheme {
                TyreTemperatureContent(value)
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
                arrayOf(
                    sampleGt7Ps5Telemetry().copy(
                        tyreTemperature =
                            Gt7Ps5TyreTemperatureData(
                                CelsiusReading(0f),
                                CelsiusReading(0f),
                                CelsiusReading(0f),
                                CelsiusReading(0f),
                            ),
                    ),
                    listOf("未取得"),
                ),
                arrayOf(sampleGt7Ps5Telemetry(), listOf("FL 69.0℃", "FR 85.0℃", "RL 100.0℃", "RR 110.0℃")),
            )
    }
}
