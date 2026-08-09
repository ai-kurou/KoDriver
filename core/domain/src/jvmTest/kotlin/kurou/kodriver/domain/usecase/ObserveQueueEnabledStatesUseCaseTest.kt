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
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveQueueEnabledStatesUseCaseTest {
    @MockK
    private lateinit var repository: QueuePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はsupportsQueue対象項目のデフォルトfalseを返す`() =
        runTest {
            every { repository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            val useCase = ObserveQueueEnabledStatesUseCase(repository)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.Flag.Root to false,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                    ReadoutItemKey.LmuWindows.PitTiming.Root to true,
                    ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
                    ReadoutItemKey.LmuWindows.TyreWear.Root to true,
                    ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
                    ReadoutItemKey.AceWindows.Flag.Root to true,
                    ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
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
            coEvery { repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true) } answers {
                states.update { it + (ReadoutItemKey.LmuWindows.Flag.Root to true) }
            }
            val useCase = ObserveQueueEnabledStatesUseCase(repository)

            repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.Flag.Root to true,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                    ReadoutItemKey.LmuWindows.PitTiming.Root to true,
                    ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
                    ReadoutItemKey.LmuWindows.TyreWear.Root to true,
                    ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
                    ReadoutItemKey.AceWindows.Flag.Root to true,
                    ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
            }
            verify(exactly = 1) { repository.observeQueueEnabledStates() }
            confirmVerified(repository)
        }
}
