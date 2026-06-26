package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
    fun `縦方向スライダーを操作するとonLongitudinalThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(),
                    onLongitudinalThresholdChanged = { changedValue = it },
                )
            }
        }

        rule.onNode(hasTestTag("vehicle_approach_longitudinal_slider"))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }

        assertNotNull(changedValue)
    }

    @Test
    fun `横方向スライダーを操作するとonLateralThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(),
                    onLateralThresholdChanged = { changedValue = it },
                )
            }
        }

        rule.onNode(hasTestTag("vehicle_approach_lateral_slider"))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }

        assertNotNull(changedValue)
    }
}
