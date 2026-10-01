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
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        LmuWindowsFlagCustomTextSpeaker(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            checkTextToSpeechAvailable,
            speakText,
        )

    private fun confirmAllMocksVerified() {
        confirmVerified(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            checkTextToSpeechAvailable,
            speakText,
        )
    }

    @Test
    fun `フラッグ以外のイベントはfalseを返しカスタム文言を参照しない`() =
        runTest {
            val result = speaker(SpeechEvent.CarLeft, VOLUME)

            assertFalse(result)
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言が空のときはfalseを返し本文を読み上げない`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("")

            val result = speaker(SpeechEvent.YellowFlag, VOLUME)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言は設定されているがTTSが利用不可のときはfalseを返し本文を読み上げない`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            val result = speaker(SpeechEvent.YellowFlag, VOLUME)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("イエロー、前方注意", volume = VOLUME) } just Runs

            val result = speaker(SpeechEvent.YellowFlag, VOLUME)

            assertTrue(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("イエロー、前方注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeBlue() } returns flowOf("ブルー、譲って")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("ブルー、譲って", volume = VOLUME) } just Runs

            val result = speaker(SpeechEvent.BlueFlag, VOLUME)

            assertTrue(result)
            coVerify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("ブルー、譲って", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeBlue() } returns flowOf(" ")

            val result = speaker(SpeechEvent.BlueFlag, VOLUME)

            assertFalse(result)
            coVerify(exactly = 1) { observeBlue() }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("フルコースイエロー、減速")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("フルコースイエロー、減速", volume = VOLUME) } just Runs

            val result = speaker(SpeechEvent.FullCourseYellow, VOLUME)

            assertTrue(result)
            coVerify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("フルコースイエロー、減速", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("")

            val result = speaker(SpeechEvent.FullCourseYellow, VOLUME)

            assertFalse(result)
            coVerify(exactly = 1) { observeFullCourseYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグの自由文字列を読み上げる`() =
        runTest {
            coEvery { observeRed() } returns flowOf("赤旗、停止")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("赤旗、停止", volume = VOLUME) } just Runs

            assertTrue(speaker(SpeechEvent.RedFlag, VOLUME))

            coVerify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("赤旗、停止", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeRed() } returns flowOf("")

            val result = speaker(SpeechEvent.RedFlag, VOLUME)

            assertFalse(result)
            coVerify(exactly = 1) { observeRed() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言は設定されているがTTSが利用不可のときはfalseを返す`() =
        runTest {
            coEvery { observeRed() } returns flowOf("赤旗、停止")
            coEvery { checkTextToSpeechAvailable() } returns false

            val result = speaker(SpeechEvent.RedFlag, VOLUME)

            assertFalse(result)
            coVerify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグは過去に収録音声が選ばれた設定が残っていても無視して文言を読み上げる`() =
        runTest {
            coEvery { observeBlue() } returns flowOf("ブルーフラッグ")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("ブルーフラッグ", volume = VOLUME) } just Runs

            val result = speaker(SpeechEvent.BlueFlag, VOLUME)

            assertTrue(result)
            coVerify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("ブルーフラッグ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `イエローフラッグは過去に収録音声が選ばれた設定が残っていても無視して文言を読み上げる`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("イエローフラッグ")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("イエローフラッグ", volume = VOLUME) } just Runs

            val result = speaker(SpeechEvent.YellowFlag, VOLUME)

            assertTrue(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("イエローフラッグ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローは過去の収録音声選択を無視して読み上げる`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("フルコースイエロー")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("フルコースイエロー", volume = VOLUME) } just Runs

            val result = speaker(SpeechEvent.FullCourseYellow, VOLUME)

            assertTrue(result)
            coVerify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("フルコースイエロー", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローはTTSが利用不可なら本文を読み上げない`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("フルコースイエロー")
            coEvery { checkTextToSpeechAvailable() } returns false

            assertFalse(speaker(SpeechEvent.FullCourseYellow, VOLUME))

            coVerify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    private companion object {
        const val VOLUME = 40
    }
}
