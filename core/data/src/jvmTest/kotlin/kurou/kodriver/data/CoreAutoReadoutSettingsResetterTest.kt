package kurou.kodriver.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class CoreAutoReadoutSettingsResetterTest {
    private val autoReadoutPreferencesRepository: AutoReadoutPreferencesRepository = mockk()
    private val queuePreferencesRepository: QueuePreferencesRepository = mockk()
    private val readoutStartSoundEnabledPreferencesRepository: ReadoutStartSoundEnabledPreferencesRepository = mockk()
    private val resetter =
        CoreAutoReadoutSettingsResetter(
            autoReadoutPreferencesRepository = autoReadoutPreferencesRepository,
            queuePreferencesRepository = queuePreferencesRepository,
            readoutStartSoundEnabledPreferencesRepository = readoutStartSoundEnabledPreferencesRepository,
        )

    @Test
    fun `対象のRepositoryをすべて一度ずつリセットする`() =
        runTest {
            coEvery { autoReadoutPreferencesRepository.resetToDefault() } returns Unit
            coEvery { queuePreferencesRepository.resetToDefault() } returns Unit
            coEvery { readoutStartSoundEnabledPreferencesRepository.resetToDefault() } returns Unit

            resetter.resetToDefault()

            coVerify(exactly = 1) { autoReadoutPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { queuePreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { readoutStartSoundEnabledPreferencesRepository.resetToDefault() }
            confirmVerified(
                autoReadoutPreferencesRepository,
                queuePreferencesRepository,
                readoutStartSoundEnabledPreferencesRepository,
            )
        }

    @Test
    fun `途中で失敗したら例外を伝播し後続を実行しない`() =
        runTest {
            val failure = IllegalStateException("reset failed")
            coEvery { autoReadoutPreferencesRepository.resetToDefault() } returns Unit
            coEvery { queuePreferencesRepository.resetToDefault() } throws failure

            assertSame(failure, assertFailsWith<IllegalStateException> { resetter.resetToDefault() })

            coVerify(exactly = 1) { autoReadoutPreferencesRepository.resetToDefault() }
            coVerify(exactly = 1) { queuePreferencesRepository.resetToDefault() }
            coVerify(exactly = 0) { readoutStartSoundEnabledPreferencesRepository.resetToDefault() }
            confirmVerified(
                autoReadoutPreferencesRepository,
                queuePreferencesRepository,
                readoutStartSoundEnabledPreferencesRepository,
            )
        }
}
