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
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveVoiceUseCaseTest {
    private val repo: VoicePreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(VOICE_ID_UNSPECIFIED)
            every { repo.voiceId() } returns state
            listOf("ja-jp-x-jab-local").forEach { voiceId ->
                coEvery { repo.saveVoiceId(voiceId) } answers { state.update { voiceId } }
            }
            val useCase = ObserveVoiceUseCase(repo)

            assertEquals(VOICE_ID_UNSPECIFIED, useCase().first())

            repo.saveVoiceId("ja-jp-x-jab-local")
            assertEquals("ja-jp-x-jab-local", useCase().first())

            verify(exactly = 2) { repo.voiceId() }
            coVerify(exactly = 1) { repo.saveVoiceId("ja-jp-x-jab-local") }
            confirmVerified(repo)
        }
}
