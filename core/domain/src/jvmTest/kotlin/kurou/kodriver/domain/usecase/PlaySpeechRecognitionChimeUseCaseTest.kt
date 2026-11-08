package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import kotlin.test.Test

class PlaySpeechRecognitionChimeUseCaseTest {
    private val player: SpeechRecognitionChimePlayer = mockk()

    @Test
    fun `確認音の再生をプレイヤーへ依頼する`() =
        runTest {
            coEvery { player.play() } returns Unit

            PlaySpeechRecognitionChimeUseCase(player)()

            coVerify(exactly = 1) { player.play() }
            confirmVerified(player)
        }
}
