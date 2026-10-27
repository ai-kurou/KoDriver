package kurou.kodriver.core.acewindowsdata

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AceWindowsAutoReadoutSettingsResetterTest {
    private val flagPreferencesRepository: AceWindowsFlagPreferencesRepository = mockk()
    private val flagReadoutTextPreferencesRepository: AceWindowsFlagReadoutTextPreferencesRepository = mockk()
    private val myBestLapPreferencesRepository: AceWindowsMyBestLapPreferencesRepository = mockk()
    private val remainingFuelLapsPreferencesRepository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk()
    private val remainingFuelPreferencesRepository: AceWindowsRemainingFuelPreferencesRepository = mockk()
    private val tyreTemperaturePreferencesRepository: AceWindowsTyreTemperaturePreferencesRepository = mockk()
    private val vehicleApproachPreferencesRepository: AceWindowsVehicleApproachPreferencesRepository = mockk()
    private val resetter =
        AceWindowsAutoReadoutSettingsResetter(
            flagPreferencesRepository = flagPreferencesRepository,
            flagReadoutTextPreferencesRepository = flagReadoutTextPreferencesRepository,
            myBestLapPreferencesRepository = myBestLapPreferencesRepository,
            remainingFuelLapsPreferencesRepository = remainingFuelLapsPreferencesRepository,
            remainingFuelPreferencesRepository = remainingFuelPreferencesRepository,
            tyreTemperaturePreferencesRepository = tyreTemperaturePreferencesRepository,
            vehicleApproachPreferencesRepository = vehicleApproachPreferencesRepository,
        )

    @Test
    fun `対象のRepositoryをすべて一度ずつリセットする`() =
        runTest {
            coEvery { flagPreferencesRepository.resetToDefault() } returns Unit
            coEvery { flagReadoutTextPreferencesRepository.resetToDefault() } returns Unit
            coEvery { myBestLapPreferencesRepository.resetToDefault() } returns Unit
            coEvery { remainingFuelLapsPreferencesRepository.resetToDefault() } returns Unit
            coEvery { remainingFuelPreferencesRepository.resetToDefault() } returns Unit
            coEvery { tyreTemperaturePreferencesRepository.resetToDefault() } returns Unit
            coEvery { vehicleApproachPreferencesRepository.resetToDefault() } returns Unit

            resetter.resetToDefault()

            coVerify(exactly = 1) { flagPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { flagReadoutTextPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { myBestLapPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { remainingFuelLapsPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { remainingFuelPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { tyreTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { vehicleApproachPreferencesRepository.resetToDefault() }
            confirmVerified(
                flagPreferencesRepository,
                flagReadoutTextPreferencesRepository,
                myBestLapPreferencesRepository,
                remainingFuelLapsPreferencesRepository,
                remainingFuelPreferencesRepository,
                tyreTemperaturePreferencesRepository,
                vehicleApproachPreferencesRepository,
            )
        }

    @Test
    fun `途中で失敗したら例外を伝播し後続を実行しない`() =
        runTest {
            val failure = IllegalStateException("reset failed")
            coEvery { flagPreferencesRepository.resetToDefault() } returns Unit
            coEvery { flagReadoutTextPreferencesRepository.resetToDefault() } throws failure

            assertSame(failure, assertFailsWith<IllegalStateException> { resetter.resetToDefault() })

            coVerify(exactly = 1) { flagPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { flagReadoutTextPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { myBestLapPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { remainingFuelLapsPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { remainingFuelPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { tyreTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { vehicleApproachPreferencesRepository.resetToDefault() }
            confirmVerified(
                flagPreferencesRepository,
                flagReadoutTextPreferencesRepository,
                myBestLapPreferencesRepository,
                remainingFuelLapsPreferencesRepository,
                remainingFuelPreferencesRepository,
                tyreTemperaturePreferencesRepository,
                vehicleApproachPreferencesRepository,
            )
        }
}
