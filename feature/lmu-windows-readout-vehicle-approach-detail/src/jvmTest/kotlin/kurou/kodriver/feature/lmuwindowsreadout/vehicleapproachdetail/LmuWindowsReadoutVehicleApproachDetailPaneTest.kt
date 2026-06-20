package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutVehicleApproachDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `縦方向スライダーを変更するとonLongitudinalThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(
                        longitudinalThresholdMeters = 1.0,
                    ),
                    onLongitudinalThresholdChanged = { changedValue = it },
                )
            }
        }

        rule.onNode(hasSliderWithRange(0.1f..10f))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5.0f) }

        assertEquals(5.0, changedValue)
    }

    @Test
    fun `横方向スライダーを変更するとonLateralThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(
                        lateralThresholdMeters = 5.0,
                    ),
                    onLateralThresholdChanged = { changedValue = it },
                )
            }
        }

        rule.onNode(hasSliderWithRange(2f..8f))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(4.0f) }

        assertEquals(4.0, changedValue)
    }

    private fun hasSliderWithRange(range: ClosedFloatingPointRange<Float>): SemanticsMatcher =
        SemanticsMatcher("Slider with range $range") { node ->
            SemanticsProperties.ProgressBarRangeInfo in node.config &&
                node.config[SemanticsProperties.ProgressBarRangeInfo].range == range
        }
}
