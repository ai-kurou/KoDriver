package kurou.kodriver.domain.microphone.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository
import kotlin.test.Test

class SaveMicrophoneCutQuietSoundInCommandUseCaseTest {
    private val repository: MicrophoneCutQuietSoundPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `指定した値をリポジトリへ保存する`() =
        runTest {
            val useCase = SaveMicrophoneCutQuietSoundInCommandUseCase(repository)

            useCase(true)
            useCase(false)

            coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundInCommand(true) }
            coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundInCommand(false) }
            confirmVerified(repository)
        }
}
