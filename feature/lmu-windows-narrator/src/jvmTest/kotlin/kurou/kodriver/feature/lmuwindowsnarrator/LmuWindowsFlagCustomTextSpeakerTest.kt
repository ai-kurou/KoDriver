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
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsFlagCustomTextSpeakerTest {
    private val observeSectorYellow: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase = mockk()
    private val observeBlue: ObserveLmuWindowsBlueFlagReadoutTextUseCase = mockk()
    private val observeFullCourseYellow: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase = mockk()
    private val observeRed: ObserveLmuWindowsRedFlagReadoutTextUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        LmuWindowsFlagCustomTextSpeaker(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            speakText,
        )

    @Test
    fun `フラッグ以外のイベントはfalseを返しカスタム文言を参照しない`() =
        runTest {
            val result = speaker(SpeechEvent.CarLeft)

            assertFalse(result)
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `セクターイエローのカスタム文言が空のときはfalseを返しWAVでの読み上げに任せる`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("")

            val result = speaker(SpeechEvent.YellowFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `セクターイエローのカスタム文言が設定されているときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { speakText("イエロー、前方注意") } just Runs

            val result = speaker(SpeechEvent.YellowFlag)

            assertTrue(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { speakText("イエロー、前方注意") }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `ブルーフラッグのカスタム文言が設定されているときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeBlue() } returns flowOf("ブルー、譲って")
            coEvery { speakText("ブルー、譲って") } just Runs

            val result = speaker(SpeechEvent.BlueFlag)

            assertTrue(result)
            coVerify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { speakText("ブルー、譲って") }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `ブルーフラッグのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeBlue() } returns flowOf(" ")

            val result = speaker(SpeechEvent.BlueFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeBlue() }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `フルコースイエローのカスタム文言が設定されているときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("フルコースイエロー、減速")
            coEvery { speakText("フルコースイエロー、減速") } just Runs

            val result = speaker(SpeechEvent.FullCourseYellow)

            assertTrue(result)
            coVerify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { speakText("フルコースイエロー、減速") }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `フルコースイエローのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("")

            val result = speaker(SpeechEvent.FullCourseYellow)

            assertFalse(result)
            coVerify(exactly = 1) { observeFullCourseYellow() }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `レッドフラッグとセッション停止は同じカスタム文言を読み上げる`() =
        runTest {
            coEvery { observeRed() } returns flowOf("赤旗、停止")
            coEvery { speakText("赤旗、停止") } just Runs

            assertTrue(speaker(SpeechEvent.RedFlag))
            assertTrue(speaker(SpeechEvent.SessionStop))

            coVerify(exactly = 2) { observeRed() }
            coVerify(exactly = 2) { speakText("赤旗、停止") }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }

    @Test
    fun `レッドフラッグのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeRed() } returns flowOf("")

            val result = speaker(SpeechEvent.RedFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeRed() }
            confirmVerified(observeSectorYellow, observeBlue, observeFullCourseYellow, observeRed, speakText)
        }
}
