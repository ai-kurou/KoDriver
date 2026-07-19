package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveSoundVolumeUseCaseTest {

    @Test
    fun `0から100の値を保存できる`() = runBlocking {
        val repository = mockk<SoundVolumePreferencesRepository>(relaxUnitFun = true)
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
        val repository = mockk<SoundVolumePreferencesRepository>(relaxUnitFun = true)

        assertFailsWith<IllegalArgumentException> { SaveSoundVolumeUseCase(repository)(-1) }

        confirmVerified(repository)
    }

    @Test
    fun `100超はIllegalArgumentExceptionをスローする`() = runBlocking {
        val repository = mockk<SoundVolumePreferencesRepository>(relaxUnitFun = true)

        assertFailsWith<IllegalArgumentException> { SaveSoundVolumeUseCase(repository)(101) }

        confirmVerified(repository)
    }
}
