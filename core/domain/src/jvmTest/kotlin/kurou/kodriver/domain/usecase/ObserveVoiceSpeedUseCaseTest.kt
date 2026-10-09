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
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveVoiceSpeedUseCaseTest {
    private val repo: VoiceSpeedPreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(1.0f)
            every { repo.voiceSpeed() } returns state
            listOf(1.25f).forEach { voiceSpeed ->
                coEvery { repo.saveVoiceSpeed(voiceSpeed) } answers { state.update { voiceSpeed } }
            }
            val useCase = ObserveVoiceSpeedUseCase(repo)

            assertEquals(1.0f, useCase().first())

            repo.saveVoiceSpeed(1.25f)
            assertEquals(1.25f, useCase().first())

            verify(exactly = 2) { repo.voiceSpeed() }
            coVerify(exactly = 1) { repo.saveVoiceSpeed(1.25f) }
            confirmVerified(repo)
        }
}
