package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleApproachPreferencesUseCasesTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk()

    @Test
    fun `SkipFirstLapの設定を取得し保存する`() =
        runTest {
            every { repository.observeSkipFirstLap() } returns flowOf(false)
            coEvery { repository.saveSkipFirstLap(false) } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals(false, useCases.observeSkipFirstLap().first())
            useCases.saveSkipFirstLap(false)

            verify(exactly = 1) { repository.observeSkipFirstLap() }
            coVerify(exactly = 1) { repository.saveSkipFirstLap(false) }
            confirmVerified(repository)
        }

    @Test
    fun `StartLeftReadoutTextの設定を取得し保存する`() =
        runTest {
            every { repository.observeStartLeftReadoutText() } returns flowOf("左注意")
            coEvery { repository.saveStartLeftReadoutText("左注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals("左注意", useCases.observeStartLeftReadoutText().first())
            useCases.saveStartLeftReadoutText("左注意")

            verify(exactly = 1) { repository.observeStartLeftReadoutText() }
            coVerify(exactly = 1) { repository.saveStartLeftReadoutText("左注意") }
            confirmVerified(repository)
        }

    @Test
    fun `StartRightReadoutTextの設定を取得し保存する`() =
        runTest {
            every { repository.observeStartRightReadoutText() } returns flowOf("右注意")
            coEvery { repository.saveStartRightReadoutText("右注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals("右注意", useCases.observeStartRightReadoutText().first())
            useCases.saveStartRightReadoutText("右注意")

            verify(exactly = 1) { repository.observeStartRightReadoutText() }
            coVerify(exactly = 1) { repository.saveStartRightReadoutText("右注意") }
            confirmVerified(repository)
        }

    @Test
    fun `SustainedReadoutTypeの設定を取得し保存する`() =
        runTest {
            every { repository.observeSustainedReadoutType() } returns
                flowOf(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)
            coEvery {
                repository.saveSustainedReadoutType(
                    VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
                )
            } returns
                Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals(
                VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
                useCases.observeSustainedReadoutType().first(),
            )
            useCases.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)

            verify(exactly = 1) { repository.observeSustainedReadoutType() }
            coVerify(
                exactly = 1,
            ) { repository.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED) }
            confirmVerified(repository)
        }
}
