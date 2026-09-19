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
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSoundVolumeUseCaseTest {
    private val repo: SoundVolumePreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(80)
            every { repo.volume() } returns state
            listOf(50).forEach { volume ->
                coEvery { repo.saveVolume(volume) } answers { state.update { volume } }
            }
            val useCase = ObserveSoundVolumeUseCase(repo)

            assertEquals(80, useCase().first())

            repo.saveVolume(50)
            assertEquals(50, useCase().first())

            verify(exactly = 2) { repo.volume() }
            coVerify(exactly = 1) { repo.saveVolume(50) }
            confirmVerified(repo)
        }
}
