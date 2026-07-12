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
import kotlin.test.assertFailsWith

private fun createSoundVolumePreferencesRepository(initial: Int = 100): SoundVolumePreferencesRepository {
    val repository = mockk<SoundVolumePreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.volume() } returns state
    coEvery { repository.saveVolume(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveSoundVolumeUseCaseTest {

    private val repo = createSoundVolumePreferencesRepository()
    private val useCase = SaveSoundVolumeUseCase(repo)

    @Test
    fun `0から100の値を保存できる`() = runBlocking {
        useCase(0)
        assertEquals(0, repo.volume().first())

        useCase(50)
        assertEquals(50, repo.volume().first())

        useCase(100)
        assertEquals(100, repo.volume().first())
    }

    @Test
    fun `0未満はIllegalArgumentExceptionをスローする`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> { useCase(-1) }
    }

    @Test
    fun `100超はIllegalArgumentExceptionをスローする`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> { useCase(101) }
    }
}
