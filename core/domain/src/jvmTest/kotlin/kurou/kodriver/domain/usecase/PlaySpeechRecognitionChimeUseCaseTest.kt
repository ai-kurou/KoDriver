package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import kurou.kodriver.domain.model.SpeechRecognitionChime
import kotlin.test.Test

class PlaySpeechRecognitionChimeUseCaseTest {
    private val player: SpeechRecognitionChimePlayer = mockk()

    @Test
    fun `指定した確認音の再生をプレイヤーへ依頼する`() =
        runTest {
            coEvery { player.play(SpeechRecognitionChime.WakeWordDetected) } returns Unit
            coEvery { player.play(SpeechRecognitionChime.CommandTimedOut) } returns Unit

            PlaySpeechRecognitionChimeUseCase(player)(SpeechRecognitionChime.WakeWordDetected)
            PlaySpeechRecognitionChimeUseCase(player)(SpeechRecognitionChime.CommandTimedOut)

            coVerify(exactly = 1) { player.play(SpeechRecognitionChime.WakeWordDetected) }
            coVerify(exactly = 1) { player.play(SpeechRecognitionChime.CommandTimedOut) }
            confirmVerified(player)
        }
}
