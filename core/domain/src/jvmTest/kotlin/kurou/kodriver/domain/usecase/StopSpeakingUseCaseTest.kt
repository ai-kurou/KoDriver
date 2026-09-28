package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kotlin.test.Test

class StopSpeakingUseCaseTest {
    private val repository: TextToSpeechRepository = mockk(relaxUnitFun = true)

    @Test
    fun `Repositoryのstopを呼び出す`() =
        runTest {
            StopSpeakingUseCase(repository)()

            coVerify(exactly = 1) { repository.stop() }
            confirmVerified(repository)
        }
}
