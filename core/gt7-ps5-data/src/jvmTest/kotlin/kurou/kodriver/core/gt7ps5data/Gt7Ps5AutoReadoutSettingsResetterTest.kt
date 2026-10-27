package kurou.kodriver.core.gt7ps5data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class Gt7Ps5AutoReadoutSettingsResetterTest {
    private val myBestLapPreferencesRepository: Gt7Ps5MyBestLapPreferencesRepository = mockk()
    private val remainingFuelLapsPreferencesRepository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk()
    private val remainingFuelPreferencesRepository: Gt7Ps5RemainingFuelPreferencesRepository = mockk()
    private val tyreTemperaturePreferencesRepository: Gt7Ps5TyreTemperaturePreferencesRepository = mockk()
    private val resetter =
        Gt7Ps5AutoReadoutSettingsResetter(
            myBestLapPreferencesRepository = myBestLapPreferencesRepository,
            remainingFuelLapsPreferencesRepository = remainingFuelLapsPreferencesRepository,
            remainingFuelPreferencesRepository = remainingFuelPreferencesRepository,
            tyreTemperaturePreferencesRepository = tyreTemperaturePreferencesRepository,
        )

    @Test
    fun `対象のRepositoryをすべて一度ずつリセットする`() =
        runTest {
            coEvery { myBestLapPreferencesRepository.resetToDefault() } returns Unit
            coEvery { remainingFuelLapsPreferencesRepository.resetToDefault() } returns Unit
            coEvery { remainingFuelPreferencesRepository.resetToDefault() } returns Unit
            coEvery { tyreTemperaturePreferencesRepository.resetToDefault() } returns Unit

            resetter.resetToDefault()

            coVerify(exactly = 1) { myBestLapPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { remainingFuelLapsPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { remainingFuelPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { tyreTemperaturePreferencesRepository.resetToDefault() }
            confirmVerified(
                myBestLapPreferencesRepository,
                remainingFuelLapsPreferencesRepository,
                remainingFuelPreferencesRepository,
                tyreTemperaturePreferencesRepository,
            )
        }

    @Test
    fun `途中で失敗したら例外を伝播し後続を実行しない`() =
        runTest {
            val failure = IllegalStateException("reset failed")
            coEvery { myBestLapPreferencesRepository.resetToDefault() } returns Unit
            coEvery { remainingFuelLapsPreferencesRepository.resetToDefault() } throws failure

            assertSame(failure, assertFailsWith<IllegalStateException> { resetter.resetToDefault() })

            coVerify(exactly = 1) { myBestLapPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { remainingFuelLapsPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { remainingFuelPreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { tyreTemperaturePreferencesRepository.resetToDefault() }
            confirmVerified(
                myBestLapPreferencesRepository,
                remainingFuelLapsPreferencesRepository,
                remainingFuelPreferencesRepository,
                tyreTemperaturePreferencesRepository,
            )
        }
}
