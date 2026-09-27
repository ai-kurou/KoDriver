package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SOUND_VOLUME_MAX
import kurou.kodriver.domain.model.SOUND_VOLUME_MIN
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveSoundVolumeUseCaseTest {
    private val repository: SoundVolumePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `0から100の値を保存できる`() =
        runTest {
            val useCase = SaveSoundVolumeUseCase(repository)

            useCase(SOUND_VOLUME_MIN)
            useCase(50)
            useCase(SOUND_VOLUME_MAX)

            coVerify(exactly = 1) { repository.saveVolume(SOUND_VOLUME_MIN) }
            coVerify(exactly = 1) { repository.saveVolume(50) }
            coVerify(exactly = 1) { repository.saveVolume(SOUND_VOLUME_MAX) }
            confirmVerified(repository)
        }

    @Test
    fun `0未満はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveSoundVolumeUseCase(repository)(SOUND_VOLUME_MIN - 1) }

            confirmVerified(repository)
        }

    @Test
    fun `100超はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveSoundVolumeUseCase(repository)(SOUND_VOLUME_MAX + 1) }

            confirmVerified(repository)
        }
}
