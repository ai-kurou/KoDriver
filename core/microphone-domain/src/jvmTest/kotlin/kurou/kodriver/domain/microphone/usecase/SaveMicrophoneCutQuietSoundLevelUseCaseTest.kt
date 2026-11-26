package kurou.kodriver.domain.microphone.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveMicrophoneCutQuietSoundLevelUseCaseTest {
    private val repository: MicrophoneCutQuietSoundPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `範囲内の値を保存できる`() =
        runTest {
            val useCase = SaveMicrophoneCutQuietSoundLevelUseCase(repository)

            useCase(MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN)
            useCase(6)
            useCase(MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX)

            coVerify(exactly = 1) {
                repository.saveMicrophoneCutQuietSoundLevel(MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN)
            }
            coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundLevel(6) }
            coVerify(exactly = 1) {
                repository.saveMicrophoneCutQuietSoundLevel(MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX)
            }
            confirmVerified(repository)
        }

    @Test
    fun `下限未満はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                SaveMicrophoneCutQuietSoundLevelUseCase(repository)(MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN - 1)
            }

            confirmVerified(repository)
        }

    @Test
    fun `上限超はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                SaveMicrophoneCutQuietSoundLevelUseCase(repository)(MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX + 1)
            }

            confirmVerified(repository)
        }
}
