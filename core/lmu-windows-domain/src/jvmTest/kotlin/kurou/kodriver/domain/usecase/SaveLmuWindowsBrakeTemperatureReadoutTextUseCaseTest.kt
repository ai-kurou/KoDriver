package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsBrakeTemperatureReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsBrakeTemperatureReadoutTextUseCase(repository)("  温度{celsius}℃  ")

            coVerify(exactly = 1) { repository.saveReadoutText("温度{celsius}℃") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は空文字として保存する`() =
        runTest {
            SaveLmuWindowsBrakeTemperatureReadoutTextUseCase(repository)(" \t\n ")

            coVerify(exactly = 1) { repository.saveReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "  " + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5) + "  "

            SaveLmuWindowsBrakeTemperatureReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
