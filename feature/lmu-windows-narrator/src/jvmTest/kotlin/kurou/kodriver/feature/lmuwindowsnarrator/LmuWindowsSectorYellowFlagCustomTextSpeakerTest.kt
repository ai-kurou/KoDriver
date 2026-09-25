package kurou.kodriver.feature.lmuwindowsnarrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsSectorYellowFlagCustomTextSpeakerTest {
    private val observeSectorYellowFlagReadoutText: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()

    @Test
    fun `イエローフラッグ以外のイベントはfalseを返しカスタム文言を参照しない`() =
        runTest {
            val speaker = LmuWindowsSectorYellowFlagCustomTextSpeaker(observeSectorYellowFlagReadoutText, speakText)

            val result = speaker(SpeechEvent.BlueFlag)

            assertFalse(result)
            confirmVerified(observeSectorYellowFlagReadoutText, speakText)
        }

    @Test
    fun `カスタム文言が空のときはfalseを返しWAVでの読み上げに任せる`() =
        runTest {
            coEvery { observeSectorYellowFlagReadoutText() } returns flowOf("")
            val speaker = LmuWindowsSectorYellowFlagCustomTextSpeaker(observeSectorYellowFlagReadoutText, speakText)

            val result = speaker(SpeechEvent.YellowFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellowFlagReadoutText() }
            confirmVerified(observeSectorYellowFlagReadoutText, speakText)
        }

    @Test
    fun `カスタム文言が設定されているときはtrueを返しOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeSectorYellowFlagReadoutText() } returns flowOf("イエロー、前方注意")
            coEvery { speakText("イエロー、前方注意") } just Runs
            val speaker = LmuWindowsSectorYellowFlagCustomTextSpeaker(observeSectorYellowFlagReadoutText, speakText)

            val result = speaker(SpeechEvent.YellowFlag)

            assertTrue(result)
            coVerify(exactly = 1) { observeSectorYellowFlagReadoutText() }
            coVerify(exactly = 1) { speakText("イエロー、前方注意") }
            confirmVerified(observeSectorYellowFlagReadoutText, speakText)
        }
}
