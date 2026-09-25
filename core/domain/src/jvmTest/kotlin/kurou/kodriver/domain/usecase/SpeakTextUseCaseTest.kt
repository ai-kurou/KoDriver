package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kotlin.test.Test

class SpeakTextUseCaseTest {
    private val repository: TextToSpeechRepository = mockk(relaxUnitFun = true)

    @Test
    fun `テキストをそのままRepositoryへ渡す`() =
        runTest {
            SpeakTextUseCase(repository)("ベストラップ")

            coVerify(exactly = 1) { repository.speak("ベストラップ", false) }
            confirmVerified(repository)
        }

    @Test
    fun `queueを指定した場合はそのままRepositoryへ渡す`() =
        runTest {
            SpeakTextUseCase(repository)("ベストラップ", queue = true)

            coVerify(exactly = 1) { repository.speak("ベストラップ", true) }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみのテキストは読み上げない`() =
        runTest {
            SpeakTextUseCase(repository)("  ")

            confirmVerified(repository)
        }
}
