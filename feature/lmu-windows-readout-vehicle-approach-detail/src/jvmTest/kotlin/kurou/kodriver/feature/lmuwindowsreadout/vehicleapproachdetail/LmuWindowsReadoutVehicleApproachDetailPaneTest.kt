package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutVehicleApproachDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ヘルプボタンをタップするとヘルプシートが表示される`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(),
                )
            }
        }

        rule.onNode(hasTestTag("vehicle_approach_help_button")).performClick()

        rule.onNode(hasTestTag("vehicle_approach_help_sheet")).assertIsDisplayed()
    }

    @Test
    fun `左接近・右接近チップをタップするとonStartReadoutTypeChangedが呼ばれる`() {
        var changedType: VehicleApproachStartReadoutType? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(
                        startReadoutEnabled = true,
                        startReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
                    ),
                    onStartReadoutTypeChanged = { changedType = it },
                )
            }
        }

        rule.onNode(hasText("左接近・右接近")).performClick()

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, changedType)
    }

    @Test
    fun `縦方向スライダーの値を確定するとonLongitudinalThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(longitudinalThresholdMeters = 1.0),
                    onLongitudinalThresholdChanged = { changedValue = it },
                )
            }
        }

        rule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 1.0f, range = 0.1f..10f, steps = 98)),
        ).performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }

        assertEquals(5.0, changedValue)
    }

    @Test
    fun `横方向スライダーの値を確定するとonLateralThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(lateralThresholdMeters = 5.0),
                    onLateralThresholdChanged = { changedValue = it },
                )
            }
        }

        rule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 5.0f, range = 2f..8f, steps = 59)),
        ).performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }

        assertEquals(4.0, changedValue)
    }
}
