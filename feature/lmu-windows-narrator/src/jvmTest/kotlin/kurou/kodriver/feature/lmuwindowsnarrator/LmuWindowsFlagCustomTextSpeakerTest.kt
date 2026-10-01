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
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase
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
    private val observeRecordedVoiceSelected: ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase = mockk()
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        LmuWindowsFlagCustomTextSpeaker(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            observeRecordedVoiceSelected,
            checkTextToSpeechAvailable,
            speakText,
        )

    private fun confirmAllMocksVerified() {
        confirmVerified(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            observeRecordedVoiceSelected,
            checkTextToSpeechAvailable,
            speakText,
        )
    }

    @Test
    fun `フラッグ以外のイベントはfalseを返しカスタム文言を参照しない`() =
        runTest {
            val result = speaker(SpeechEvent.CarLeft)

            assertFalse(result)
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言が空のときはfalseを返しWAVでの読み上げに任せる`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("")

            val result = speaker(SpeechEvent.YellowFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言は設定されているがTTSが利用不可のときはfalseを返しWAVでの読み上げに任せる`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG) } returns
                flowOf(false)
            coEvery { checkTextToSpeechAvailable() } returns false

            val result = speaker(SpeechEvent.YellowFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG) }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG) } returns
                flowOf(false)
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("イエロー、前方注意") } just Runs

            val result = speaker(SpeechEvent.YellowFlag)

            assertTrue(result)
            coVerify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG) }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("イエロー、前方注意") }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeBlue() } returns flowOf("ブルー、譲って")
            coEvery { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.BLUE_FLAG) } returns flowOf(false)
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("ブルー、譲って") } just Runs

            val result = speaker(SpeechEvent.BlueFlag)

            assertTrue(result)
            coVerify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.BLUE_FLAG) }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("ブルー、譲って") }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeBlue() } returns flowOf(" ")

            val result = speaker(SpeechEvent.BlueFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeBlue() }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("フルコースイエロー、減速")
            coEvery { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW) } returns
                flowOf(false)
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("フルコースイエロー、減速") } just Runs

            val result = speaker(SpeechEvent.FullCourseYellow)

            assertTrue(result)
            coVerify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW) }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("フルコースイエロー、減速") }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeFullCourseYellow() } returns flowOf("")

            val result = speaker(SpeechEvent.FullCourseYellow)

            assertFalse(result)
            coVerify(exactly = 1) { observeFullCourseYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグとセッション停止は同じカスタム文言を読み上げる`() =
        runTest {
            coEvery { observeRed() } returns flowOf("赤旗、停止")
            coEvery { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.RED_FLAG) } returns flowOf(false)
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("赤旗、停止") } just Runs

            assertTrue(speaker(SpeechEvent.RedFlag))
            assertTrue(speaker(SpeechEvent.SessionStop))

            coVerify(exactly = 2) { observeRed() }
            coVerify(exactly = 2) { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.RED_FLAG) }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 2) { speakText("赤旗、停止") }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言が空のときはfalseを返す`() =
        runTest {
            coEvery { observeRed() } returns flowOf("")

            val result = speaker(SpeechEvent.RedFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeRed() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言は設定されているがTTSが利用不可のときはfalseを返す`() =
        runTest {
            coEvery { observeRed() } returns flowOf("赤旗、停止")
            coEvery { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.RED_FLAG) } returns flowOf(false)
            coEvery { checkTextToSpeechAvailable() } returns false

            val result = speaker(SpeechEvent.RedFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.RED_FLAG) }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `収録音声が明示的に選ばれているときは文言が残っていてもfalseを返しTTSを確認しない`() =
        runTest {
            coEvery { observeBlue() } returns flowOf("ブルー、譲って")
            coEvery { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.BLUE_FLAG) } returns flowOf(true)

            val result = speaker(SpeechEvent.BlueFlag)

            assertFalse(result)
            coVerify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { observeRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.BLUE_FLAG) }
            confirmAllMocksVerified()
        }
}
