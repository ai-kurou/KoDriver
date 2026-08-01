package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSoundVolumeUseCaseTest {

    @MockK
    private lateinit var repo: SoundVolumePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runBlocking {
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
