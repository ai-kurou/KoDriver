package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SaveSoundVolumeUseCaseTest {

    private val repo = FakeSoundVolumePreferencesRepository()
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
