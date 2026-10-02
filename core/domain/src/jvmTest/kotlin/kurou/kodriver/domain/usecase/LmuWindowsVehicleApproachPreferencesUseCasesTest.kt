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
    fun `SustainedLeftReadoutTextの設定を取得し保存する`() =
        runTest {
            every { repository.observeSustainedLeftReadoutText() } returns flowOf("左注意")
            coEvery { repository.saveSustainedLeftReadoutText("左注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals("左注意", useCases.observeSustainedLeftReadoutText().first())
            useCases.saveSustainedLeftReadoutText("左注意")

            verify(exactly = 1) { repository.observeSustainedLeftReadoutText() }
            coVerify(exactly = 1) { repository.saveSustainedLeftReadoutText("左注意") }
            confirmVerified(repository)
        }

    @Test
    fun `SustainedRightReadoutTextの設定を取得し保存する`() =
        runTest {
            every { repository.observeSustainedRightReadoutText() } returns flowOf("右注意")
            coEvery { repository.saveSustainedRightReadoutText("右注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals("右注意", useCases.observeSustainedRightReadoutText().first())
            useCases.saveSustainedRightReadoutText("右注意")

            verify(exactly = 1) { repository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { repository.saveSustainedRightReadoutText("右注意") }
            confirmVerified(repository)
        }
}
