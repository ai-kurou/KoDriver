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
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveLowVolumeWarningEnabledUseCaseTest {
    private val repository: LowVolumeWarningEnabledPreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(true)
            every { repository.observeLowVolumeWarningEnabled() } returns state
            coEvery { repository.saveLowVolumeWarningEnabled(false) } answers { state.update { false } }
            val useCase = ObserveLowVolumeWarningEnabledUseCase(repository)

            assertTrue(useCase().first())

            repository.saveLowVolumeWarningEnabled(false)
            assertFalse(useCase().first())

            verify(exactly = 2) { repository.observeLowVolumeWarningEnabled() }
            coVerify(exactly = 1) { repository.saveLowVolumeWarningEnabled(false) }
            confirmVerified(repository)
        }
}
