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
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveQueueEnabledStatesUseCaseTest {
    private val repository: QueuePreferencesRepository = mockk()

    @Test
    fun `初期値はsupportsQueue対象項目のデフォルトfalseを返す`() =
        runTest {
            every { repository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            val useCase = ObserveQueueEnabledStatesUseCase(repository)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.Flag.Root to false,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                    LmuWindowsReadoutItemKey.PitTiming.Root to true,
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
                    LmuWindowsReadoutItemKey.TyreWear.Root to true,
                    LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
                    LmuWindowsReadoutItemKey.BrakeWear.Root to true,
                    LmuWindowsReadoutItemKey.MyBestLap.Root to false,
                    Gt7Ps5ReadoutItemKey.MyBestLap.Root to false,
                    Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
                    Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
                    Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
                    AceWindowsReadoutItemKey.Flag.Root to false,
                    AceWindowsReadoutItemKey.RemainingFuel.Root to true,
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
                    AceWindowsReadoutItemKey.TyreTemperature.Root to true,
                    AceWindowsReadoutItemKey.MyBestLap.Root to false,
                ),
                useCase().first(),
            )
            verify(exactly = 1) { repository.observeQueueEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeQueueEnabledStates() } returns states
            coEvery { repository.saveQueueEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true) } answers {
                states.update { it + (LmuWindowsReadoutItemKey.Flag.Root to true) }
            }
            val useCase = ObserveQueueEnabledStatesUseCase(repository)

            repository.saveQueueEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.Flag.Root to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                    LmuWindowsReadoutItemKey.PitTiming.Root to true,
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
                    LmuWindowsReadoutItemKey.TyreWear.Root to true,
                    LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
                    LmuWindowsReadoutItemKey.BrakeWear.Root to true,
                    LmuWindowsReadoutItemKey.MyBestLap.Root to false,
                    Gt7Ps5ReadoutItemKey.MyBestLap.Root to false,
                    Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
                    Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
                    Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
                    AceWindowsReadoutItemKey.Flag.Root to false,
                    AceWindowsReadoutItemKey.RemainingFuel.Root to true,
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
                    AceWindowsReadoutItemKey.TyreTemperature.Root to true,
                    AceWindowsReadoutItemKey.MyBestLap.Root to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repository.saveQueueEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true)
            }
            verify(exactly = 1) { repository.observeQueueEnabledStates() }
            confirmVerified(repository)
        }
}
