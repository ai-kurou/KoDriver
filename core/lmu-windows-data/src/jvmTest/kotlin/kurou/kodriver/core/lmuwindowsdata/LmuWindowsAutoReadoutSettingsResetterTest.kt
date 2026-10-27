package kurou.kodriver.core.lmuwindowsdata

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class LmuWindowsAutoReadoutSettingsResetterTest {
    private val flagPreferencesRepository: LmuWindowsFlagPreferencesRepository = mockk()
    private val flagReadoutTextPreferencesRepository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()
    private val myBestLapPreferencesRepository: LmuWindowsMyBestLapPreferencesRepository = mockk()
    private val pitTimingPreferencesRepository: LmuWindowsPitTimingPreferencesRepository = mockk()
    private val remainingFuelPreferencesRepository: LmuWindowsRemainingFuelPreferencesRepository = mockk()
    private val remainingVirtualEnergyPreferencesRepository: LmuWindowsRemainingVirtualEnergyPreferencesRepository =
        mockk()
    private val tyreTemperaturePreferencesRepository: LmuWindowsTyreTemperaturePreferencesRepository = mockk()
    private val tyreWearPreferencesRepository: LmuWindowsTyreWearPreferencesRepository = mockk()
    private val vehicleApproachPreferencesRepository: LmuWindowsVehicleApproachPreferencesRepository = mockk()
    private val thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository =
        mockk()
    private val vehicleClassBrakeTemperaturePreferencesRepository:
        LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk()
    private val vehicleClassBrakeWearPreferencesRepository: LmuWindowsVehicleClassBrakeWearPreferencesRepository =
        mockk()
    private val vehicleClassTyreTemperaturePreferencesRepository:
        LmuWindowsVehicleClassTyreTemperaturePreferencesRepository = mockk()
    private val vehicleDamagePreferencesRepository: LmuWindowsVehicleDamagePreferencesRepository = mockk()
    private val resetter =
        LmuWindowsAutoReadoutSettingsResetter(
            flagPreferencesRepository = flagPreferencesRepository,
            flagReadoutTextPreferencesRepository = flagReadoutTextPreferencesRepository,
            myBestLapPreferencesRepository = myBestLapPreferencesRepository,
            pitTimingPreferencesRepository = pitTimingPreferencesRepository,
            remainingFuelPreferencesRepository = remainingFuelPreferencesRepository,
            remainingVirtualEnergyPreferencesRepository = remainingVirtualEnergyPreferencesRepository,
            tyreTemperaturePreferencesRepository = tyreTemperaturePreferencesRepository,
            tyreWearPreferencesRepository = tyreWearPreferencesRepository,
            vehicleApproachPreferencesRepository = vehicleApproachPreferencesRepository,
            vehicleApproachThresholdsPreferencesRepository = thresholdsRepository,
            vehicleClassBrakeTemperaturePreferencesRepository = vehicleClassBrakeTemperaturePreferencesRepository,
            vehicleClassBrakeWearPreferencesRepository = vehicleClassBrakeWearPreferencesRepository,
            vehicleClassTyreTemperaturePreferencesRepository = vehicleClassTyreTemperaturePreferencesRepository,
            vehicleDamagePreferencesRepository = vehicleDamagePreferencesRepository,
        )

    @Test
    fun `対象のRepositoryをすべて一度ずつリセットする`() =
        runTest {
            coEvery { flagPreferencesRepository.resetToDefault() } returns Unit
            coEvery { flagReadoutTextPreferencesRepository.resetToDefault() } returns Unit
            coEvery { myBestLapPreferencesRepository.resetToDefault() } returns Unit
            coEvery { pitTimingPreferencesRepository.resetToDefault() } returns Unit
            coEvery { remainingFuelPreferencesRepository.resetToDefault() } returns Unit
            coEvery { remainingVirtualEnergyPreferencesRepository.resetToDefault() } returns Unit
            coEvery { tyreTemperaturePreferencesRepository.resetToDefault() } returns Unit
            coEvery { tyreWearPreferencesRepository.resetToDefault() } returns Unit
            coEvery { vehicleApproachPreferencesRepository.resetToDefault() } returns Unit
            coEvery { thresholdsRepository.resetToDefault() } returns Unit
            coEvery { vehicleClassBrakeTemperaturePreferencesRepository.resetToDefault() } returns Unit
            coEvery { vehicleClassBrakeWearPreferencesRepository.resetToDefault() } returns Unit
            coEvery { vehicleClassTyreTemperaturePreferencesRepository.resetToDefault() } returns Unit
            coEvery { vehicleDamagePreferencesRepository.resetToDefault() } returns Unit

            resetter.resetToDefault()

            coVerify(exactly = 1) { flagPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { flagReadoutTextPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { myBestLapPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { pitTimingPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { remainingFuelPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { remainingVirtualEnergyPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { tyreTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { tyreWearPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { vehicleApproachPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { thresholdsRepository.resetToDefault() }
            coVerify(exactly = 1) { vehicleClassBrakeTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { vehicleClassBrakeWearPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { vehicleClassTyreTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { vehicleDamagePreferencesRepository.resetToDefault() }
            confirmVerified(
                flagPreferencesRepository,
                flagReadoutTextPreferencesRepository,
                myBestLapPreferencesRepository,
                pitTimingPreferencesRepository,
                remainingFuelPreferencesRepository,
                remainingVirtualEnergyPreferencesRepository,
                tyreTemperaturePreferencesRepository,
                tyreWearPreferencesRepository,
                vehicleApproachPreferencesRepository,
                thresholdsRepository,
                vehicleClassBrakeTemperaturePreferencesRepository,
                vehicleClassBrakeWearPreferencesRepository,
                vehicleClassTyreTemperaturePreferencesRepository,
                vehicleDamagePreferencesRepository,
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
            coVerify(exactly = 0) { pitTimingPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { remainingFuelPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { remainingVirtualEnergyPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { tyreTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { tyreWearPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { vehicleApproachPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { thresholdsRepository.resetToDefault() }
            coVerify(exactly = 0) { vehicleClassBrakeTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { vehicleClassBrakeWearPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { vehicleClassTyreTemperaturePreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { vehicleDamagePreferencesRepository.resetToDefault() }
            confirmVerified(
                flagPreferencesRepository,
                flagReadoutTextPreferencesRepository,
                myBestLapPreferencesRepository,
                pitTimingPreferencesRepository,
                remainingFuelPreferencesRepository,
                remainingVirtualEnergyPreferencesRepository,
                tyreTemperaturePreferencesRepository,
                tyreWearPreferencesRepository,
                vehicleApproachPreferencesRepository,
                thresholdsRepository,
                vehicleClassBrakeTemperaturePreferencesRepository,
                vehicleClassBrakeWearPreferencesRepository,
                vehicleClassTyreTemperaturePreferencesRepository,
                vehicleDamagePreferencesRepository,
            )
        }
}
