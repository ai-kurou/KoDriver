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
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsSectorYellowFlagCustomTextSpeakerTest {
    private val observeSectorYellowFlagReadoutText: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase = mockk()
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()

    private fun createSpeaker() =
        LmuWindowsSectorYellowFlagCustomTextSpeaker(
            observeSectorYellowFlagReadoutText,
            checkTextToSpeechAvailable,
            speakText,
        )

    @Test
    fun `イエローフラッグ以外のイベントはfalseを返しカスタム文言を参照しない`() =
        runTest {
            val speaker = createSpeaker()

            val result = speaker(SpeechEvent.BlueFlag)

            assertFalse(result)
            confirmVerified(observeSectorYellowFlagReadoutText, checkTextToSpeechAvailable, speakText)
        }

    @Test
    fun `カスタム文言が空のときはfalseを返しWAVでの読み上げに任せる`() =
        runTest {
            coEvery { observeSectorYellowFlagReadoutText() } returns flowOf("")
            val speaker = createSpeaker()

            val result = speaker(SpeechEvent.YellowFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellowFlagReadoutText() }
            confirmVerified(observeSectorYellowFlagReadoutText, checkTextToSpeechAvailable, speakText)
        }

    @Test
    fun `カスタム文言は設定されているがTTSが利用不可のときはfalseを返しWAVでの読み上げに任せる`() =
        runTest {
            coEvery { observeSectorYellowFlagReadoutText() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns false
            val speaker = createSpeaker()

            val result = speaker(SpeechEvent.YellowFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellowFlagReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmVerified(observeSectorYellowFlagReadoutText, checkTextToSpeechAvailable, speakText)
        }

    @Test
    fun `カスタム文言が設定されておりTTSが利用可能なときはtrueを返しOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeSectorYellowFlagReadoutText() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("イエロー、前方注意") } just Runs
            val speaker = createSpeaker()

            val result = speaker(SpeechEvent.YellowFlag)

            assertTrue(result)
            coVerify(exactly = 1) { observeSectorYellowFlagReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("イエロー、前方注意") }
            confirmVerified(observeSectorYellowFlagReadoutText, checkTextToSpeechAvailable, speakText)
        }
}
