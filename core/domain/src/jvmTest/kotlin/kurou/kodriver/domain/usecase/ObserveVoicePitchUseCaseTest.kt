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
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveVoicePitchUseCaseTest {
    private val repo: VoicePitchPreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(1.0f)
            every { repo.voicePitch() } returns state
            listOf(1.25f).forEach { voicePitch ->
                coEvery { repo.saveVoicePitch(voicePitch) } answers { state.update { voicePitch } }
            }
            val useCase = ObserveVoicePitchUseCase(repo)

            assertEquals(1.0f, useCase().first())

            repo.saveVoicePitch(1.25f)
            assertEquals(1.25f, useCase().first())

            verify(exactly = 2) { repo.voicePitch() }
            coVerify(exactly = 1) { repo.saveVoicePitch(1.25f) }
            confirmVerified(repo)
        }
}
