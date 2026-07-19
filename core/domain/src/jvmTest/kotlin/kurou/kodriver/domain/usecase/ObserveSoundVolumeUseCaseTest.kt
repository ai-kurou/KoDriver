package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSoundVolumeUseCaseTest {

    @Test
    fun `初期値を返す・保存済みの値を返す`() = runBlocking {
        val repo = mockk<SoundVolumePreferencesRepository>()
        val state = MutableStateFlow(80)
        every { repo.volume() } returns state
        listOf(50).forEach { volume ->
            coEvery { repo.saveVolume(volume) } answers { state.update { volume } }
        }
        val useCase = ObserveSoundVolumeUseCase(repo)

        assertEquals(80, useCase().first())

        repo.saveVolume(50)
        assertEquals(50, useCase().first())

        io.mockk.verify(exactly = 2) { repo.volume() }
        io.mockk.coVerify(exactly = 1) { repo.saveVolume(50) }
        io.mockk.confirmVerified(repo)
    }
}
