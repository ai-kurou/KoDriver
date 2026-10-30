package kurou.kodriver.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class CoreManualReadoutSettingsResetterTest {
    private val lmuRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val aceRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val gt7Repository: ManualReadoutSetPreferencesRepository = mockk()
    private val playButtonRepository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository = mockk()
    private val resetter =
        CoreManualReadoutSettingsResetter(
            lmuWindowsManualReadoutSetPreferencesRepository = lmuRepository,
            aceWindowsManualReadoutSetPreferencesRepository = aceRepository,
            gt7Ps5ManualReadoutSetPreferencesRepository = gt7Repository,
            manualReadoutPlayButtonLayoutEnabledPreferencesRepository = playButtonRepository,
        )

    @Test
    fun `対象のRepositoryをすべて一度ずつリセットする`() =
        runTest {
            coEvery { lmuRepository.resetToDefault() } returns Unit
            coEvery { aceRepository.resetToDefault() } returns Unit
            coEvery { gt7Repository.resetToDefault() } returns Unit
            coEvery { playButtonRepository.resetToDefault() } returns Unit

            resetter.resetToDefault()

            coVerify(exactly = 1) { lmuRepository.resetToDefault() }
            coVerify(exactly = 1) { aceRepository.resetToDefault() }
            coVerify(exactly = 1) { gt7Repository.resetToDefault() }
            coVerify(exactly = 1) { playButtonRepository.resetToDefault() }
            confirmVerified(lmuRepository, aceRepository, gt7Repository, playButtonRepository)
        }

    @Test
    fun `途中で失敗したら例外を伝播し後続を実行しない`() =
        runTest {
            val failure = IllegalStateException("reset failed")
            coEvery { lmuRepository.resetToDefault() } returns Unit
            coEvery { aceRepository.resetToDefault() } throws failure

            assertSame(failure, assertFailsWith<IllegalStateException> { resetter.resetToDefault() })

            coVerify(exactly = 1) { lmuRepository.resetToDefault() }
            coVerify(exactly = 1) { aceRepository.resetToDefault() }
            coVerify(exactly = 0) { gt7Repository.resetToDefault() }
            coVerify(exactly = 0) { playButtonRepository.resetToDefault() }
            confirmVerified(lmuRepository, aceRepository, gt7Repository, playButtonRepository)
        }
}
