package kurou.kodriver.feature.acewindowsnarrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AceWindowsReadoutTextSpeakerTest {
    private val observeText: ObserveAceWindowsCheckeredFlagReadoutTextUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker = AceWindowsReadoutTextSpeaker(observeText, checkAvailable, speakText)

    @Test
    fun `保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeText() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsCheckeredFlag))
            speaker(SpeechEvent.AceWindowsCheckeredFlag, 42)

            verify(exactly = 2) { observeText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeText, checkAvailable, speakText)
        }

    @Test
    fun `空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeText() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsCheckeredFlag))
                speaker(SpeechEvent.AceWindowsCheckeredFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeText() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeText, checkAvailable, speakText)
        }

    @Test
    fun `TTS利用不可なら読み上げない`() =
        runTest {
            every { observeText() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsCheckeredFlag))
            speaker(SpeechEvent.AceWindowsCheckeredFlag, 100)
            verify(exactly = 2) { observeText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeText, checkAvailable, speakText)
        }

    @Test
    fun `他フラッグと対象外イベントは設定を参照しない`() =
        runTest {
            val events =
                listOf(
                    SpeechEvent.AceWindowsWhiteFlag,
                    SpeechEvent.AceWindowsGreenFlag,
                    SpeechEvent.AceWindowsRedFlag,
                    SpeechEvent.AceWindowsBlueFlag,
                    SpeechEvent.AceWindowsYellowFlag,
                    SpeechEvent.AceWindowsBlackFlag,
                    SpeechEvent.AceWindowsBlackWhiteFlag,
                    SpeechEvent.AceWindowsOrangeCircleFlag,
                    SpeechEvent.AceWindowsRedYellowStripesFlag,
                    SpeechEvent.AceWindowsRemainingFuelWarning,
                )
            events.forEach { event ->
                assertFalse(isAceWindowsCustomSpeakEvent(event))
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }
            assertTrue(isAceWindowsCustomSpeakEvent(SpeechEvent.AceWindowsCheckeredFlag))
            verify(exactly = 0) { observeText() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeText, checkAvailable, speakText)
        }
}
