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
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleApproachPreferencesUseCasesTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk()
    private val readoutTextRepository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository =
        mockk(relaxUnitFun = true)

    @Test
    fun `SkipFirstLapの設定を取得し保存する`() =
        runTest {
            every { repository.observeSkipFirstLap() } returns flowOf(false)
            coEvery { repository.saveSkipFirstLap(false) } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository, readoutTextRepository)

            assertEquals(false, useCases.observeSkipFirstLap().first())
            useCases.saveSkipFirstLap(false)

            verify(exactly = 1) { repository.observeSkipFirstLap() }
            coVerify(exactly = 1) { repository.saveSkipFirstLap(false) }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `StartLeftReadoutTextの設定を取得し保存する`() =
        runTest {
            every { readoutTextRepository.observeStartLeftReadoutText() } returns flowOf("左注意")
            coEvery { readoutTextRepository.saveStartLeftReadoutText("左注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository, readoutTextRepository)

            assertEquals("左注意", useCases.observeStartLeftReadoutText().first())
            useCases.saveStartLeftReadoutText("左注意")

            verify(exactly = 1) { readoutTextRepository.observeStartLeftReadoutText() }
            coVerify(exactly = 1) { readoutTextRepository.saveStartLeftReadoutText("左注意") }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `StartRightReadoutTextの設定を取得し保存する`() =
        runTest {
            every { readoutTextRepository.observeStartRightReadoutText() } returns flowOf("右注意")
            coEvery { readoutTextRepository.saveStartRightReadoutText("右注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository, readoutTextRepository)

            assertEquals("右注意", useCases.observeStartRightReadoutText().first())
            useCases.saveStartRightReadoutText("右注意")

            verify(exactly = 1) { readoutTextRepository.observeStartRightReadoutText() }
            coVerify(exactly = 1) { readoutTextRepository.saveStartRightReadoutText("右注意") }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `SustainedLeftReadoutTextの設定を取得し保存する`() =
        runTest {
            every { readoutTextRepository.observeSustainedLeftReadoutText() } returns flowOf("左注意")
            coEvery { readoutTextRepository.saveSustainedLeftReadoutText("左注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository, readoutTextRepository)

            assertEquals("左注意", useCases.observeSustainedLeftReadoutText().first())
            useCases.saveSustainedLeftReadoutText("左注意")

            verify(exactly = 1) { readoutTextRepository.observeSustainedLeftReadoutText() }
            coVerify(exactly = 1) { readoutTextRepository.saveSustainedLeftReadoutText("左注意") }
            confirmVerified(repository, readoutTextRepository)
        }

    @Test
    fun `SustainedRightReadoutTextの設定を取得し保存する`() =
        runTest {
            every { readoutTextRepository.observeSustainedRightReadoutText() } returns flowOf("右注意")
            coEvery { readoutTextRepository.saveSustainedRightReadoutText("右注意") } returns Unit
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository, readoutTextRepository)

            assertEquals("右注意", useCases.observeSustainedRightReadoutText().first())
            useCases.saveSustainedRightReadoutText("右注意")

            verify(exactly = 1) { readoutTextRepository.observeSustainedRightReadoutText() }
            coVerify(exactly = 1) { readoutTextRepository.saveSustainedRightReadoutText("右注意") }
            confirmVerified(repository, readoutTextRepository)
        }
}
