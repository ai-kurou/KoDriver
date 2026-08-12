package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5ReadoutTyreTemperatureDetailViewModelTest {
    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            val viewModel = Gt7Ps5ReadoutTyreTemperatureDetailViewModel()

            assertEquals(Gt7Ps5ReadoutTyreTemperatureDetailUiState(), viewModel.uiState.first())
        }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() =
        runTest {
            val viewModel = Gt7Ps5ReadoutTyreTemperatureDetailViewModel()

            viewModel.onOverheatWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
        }

    @Test
    fun `onHighThresholdChangedを呼ぶとuiStateのhighThresholdCelsiusが更新される`() =
        runTest {
            val viewModel = Gt7Ps5ReadoutTyreTemperatureDetailViewModel()

            viewModel.onHighThresholdChanged(105)

            assertEquals(105, viewModel.uiState.first().highThresholdCelsius)
        }

    @Test
    fun `onHighThresholdResetを呼ぶとhighThresholdCelsiusがデフォルト値に戻る`() =
        runTest {
            val viewModel = Gt7Ps5ReadoutTyreTemperatureDetailViewModel()
            viewModel.onHighThresholdChanged(105)

            viewModel.onHighThresholdReset()

            assertEquals(
                GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                viewModel.uiState.first().highThresholdCelsius,
            )
        }
}
