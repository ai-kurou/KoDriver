@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveReadoutStartSoundEnabledStatesUseCaseTest {
    @MockK
    private lateinit var repository: ReadoutStartSoundEnabledPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はLMUとACEの車両接近のみfalseそれ以外はtrueのデフォルト値を返す`() =
        runTest {
            every { repository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val useCase = ObserveReadoutStartSoundEnabledStatesUseCase(repository)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.VehicleApproach.Root to false,
                    ReadoutItemKey.LmuWindows.Flag.Root to true,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                    ReadoutItemKey.LmuWindows.PitTiming.Root to true,
                    ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
                    ReadoutItemKey.LmuWindows.TyreWear.Root to true,
                    ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
                    ReadoutItemKey.Gt7Ps5.TyreTemperature.Root to true,
                    ReadoutItemKey.AceWindows.Flag.Root to true,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root to false,
                    ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
                    ReadoutItemKey.AceWindows.TyreTemperature.Root to true,
                    ReadoutItemKey.AceWindows.MyBestLap.Root to true,
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
                repository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)
            } answers {
                states.update { it + (ReadoutItemKey.LmuWindows.Flag.Root to false) }
            }
            val useCase = ObserveReadoutStartSoundEnabledStatesUseCase(repository)

            repository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)

            assertEquals(false, useCase().first()[ReadoutItemKey.LmuWindows.Flag.Root])
            assertEquals(true, useCase().first()[ReadoutItemKey.LmuWindows.TyreWear.Root])
            coVerify(exactly = 1) {
                repository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)
            }
            verify(exactly = 2) { repository.observeStartSoundEnabledStates() }
            confirmVerified(repository)
        }
}
