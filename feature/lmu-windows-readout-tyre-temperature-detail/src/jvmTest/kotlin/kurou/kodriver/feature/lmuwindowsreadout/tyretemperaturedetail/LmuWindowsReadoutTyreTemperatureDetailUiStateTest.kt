package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreTemperatureDetailUiStateTest {
    @Test
    fun `閾値マップが空なら全クラスで選択中クラスの既定値を返す`() {
        tyreTemperatureVehicleClasses.forEach { vehicleClass ->
            val state = LmuWindowsReadoutTyreTemperatureDetailUiState(selectedVehicleClass = vehicleClass)
            assertEquals(
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass),
                state.selectedVehicleClassHighThresholdCelsius,
            )
        }
    }

    @Test
    fun `選択中クラスの設定値を返し未設定クラスは既定値を返す`() {
        val state =
            LmuWindowsReadoutTyreTemperatureDetailUiState(
                vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Gt3 to 107),
                selectedVehicleClass = LmuWindowsVehicleClassData.Gt3,
            )
        assertEquals(Celsius(107), state.selectedVehicleClassHighThresholdCelsius)
        tyreTemperatureVehicleClasses.filter { it != LmuWindowsVehicleClassData.Gt3 }.forEach { vehicleClass ->
            assertEquals(
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass),
                state.copy(selectedVehicleClass = vehicleClass).selectedVehicleClassHighThresholdCelsius,
            )
        }
    }
}

/** sealed class の全クラスを試聴テストで共有する。 */
internal val tyreTemperatureVehicleClasses =
    listOf(
        LmuWindowsVehicleClassData.Hypercar,
        LmuWindowsVehicleClassData.P2,
        LmuWindowsVehicleClassData.P2Elms,
        LmuWindowsVehicleClassData.P3,
        LmuWindowsVehicleClassData.Gte,
        LmuWindowsVehicleClassData.Gt3,
        LmuWindowsVehicleClassData.Unknown("Unknown"),
    )
