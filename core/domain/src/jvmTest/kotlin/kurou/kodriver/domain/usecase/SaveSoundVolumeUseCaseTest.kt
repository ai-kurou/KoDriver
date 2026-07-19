package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveSoundVolumeUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: SoundVolumePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `0から100の値を保存できる`() = runBlocking {
        val useCase = SaveSoundVolumeUseCase(repository)

        useCase(0)
        useCase(50)
        useCase(100)

        coVerify(exactly = 1) { repository.saveVolume(0) }
        coVerify(exactly = 1) { repository.saveVolume(50) }
        coVerify(exactly = 1) { repository.saveVolume(100) }
        confirmVerified(repository)
    }

    @Test
    fun `0未満はIllegalArgumentExceptionをスローする`() = runBlocking {
        assertFailsWith<IllegalArgumentException> { SaveSoundVolumeUseCase(repository)(-1) }

        confirmVerified(repository)
    }

    @Test
    fun `100超はIllegalArgumentExceptionをスローする`() = runBlocking {
        assertFailsWith<IllegalArgumentException> { SaveSoundVolumeUseCase(repository)(101) }

        confirmVerified(repository)
    }
}
