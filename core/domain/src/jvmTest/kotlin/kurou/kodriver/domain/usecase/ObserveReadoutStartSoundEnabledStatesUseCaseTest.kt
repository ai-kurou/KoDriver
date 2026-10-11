package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveReadoutStartSoundEnabledStatesUseCaseTest {
    private val repository: ReadoutStartSoundEnabledPreferencesRepository = mockk()

    @Test
    fun `初期値はLMUとACEの車両接近のみfalseそれ以外はtrueのデフォルト値を返す`() =
        runTest {
            every { repository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val useCase = ObserveReadoutStartSoundEnabledStatesUseCase(repository)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root to false,
                    LmuWindowsReadoutItemKey.Flag.Root to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root to true,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                    LmuWindowsReadoutItemKey.PitTiming.Root to true,
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
                    LmuWindowsReadoutItemKey.TyreWear.Root to true,
                    LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
                    LmuWindowsReadoutItemKey.BrakeWear.Root to true,
                    LmuWindowsReadoutItemKey.MyBestLap.Root to true,
                    Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
                    Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
                    Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
                    Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
                    AceWindowsReadoutItemKey.Flag.Root to true,
                    AceWindowsReadoutItemKey.VehicleApproach.Root to false,
                    AceWindowsReadoutItemKey.RemainingFuel.Root to true,
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
                    AceWindowsReadoutItemKey.TyreTemperature.Root to true,
                    AceWindowsReadoutItemKey.MyBestLap.Root to true,
                ),
                useCase().first(),
            )
            verify(exactly = 1) { repository.observeStartSoundEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeStartSoundEnabledStates() } returns states
            coEvery {
                repository.saveStartSoundEnabledState(LmuWindowsReadoutItemKey.Flag.Root, false)
            } answers {
                states.update { it + (LmuWindowsReadoutItemKey.Flag.Root to false) }
            }
            val useCase = ObserveReadoutStartSoundEnabledStatesUseCase(repository)

            repository.saveStartSoundEnabledState(LmuWindowsReadoutItemKey.Flag.Root, false)

            assertEquals(false, useCase().first()[LmuWindowsReadoutItemKey.Flag.Root])
            assertEquals(true, useCase().first()[LmuWindowsReadoutItemKey.TyreWear.Root])
            coVerify(exactly = 1) {
                repository.saveStartSoundEnabledState(LmuWindowsReadoutItemKey.Flag.Root, false)
            }
            verify(exactly = 2) { repository.observeStartSoundEnabledStates() }
            confirmVerified(repository)
        }
}
