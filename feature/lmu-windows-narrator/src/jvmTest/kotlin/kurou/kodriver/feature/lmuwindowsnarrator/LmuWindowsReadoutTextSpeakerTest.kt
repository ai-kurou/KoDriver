package kurou.kodriver.feature.lmuwindowsnarrator

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
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test

class LmuWindowsReadoutTextSpeakerTest {
    private val observeSectorYellow: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase = mockk()
    private val observeBlue: ObserveLmuWindowsBlueFlagReadoutTextUseCase = mockk()
    private val observeFullCourseYellow: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase = mockk()
    private val observeRed: ObserveLmuWindowsRedFlagReadoutTextUseCase = mockk()
    private val observeLeft: ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase = mockk()
    private val observeRight: ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase = mockk()
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        LmuWindowsReadoutTextSpeaker(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            observeLeft,
            observeRight,
            checkTextToSpeechAvailable,
            speakText,
        )

    private fun confirmAllMocksVerified() {
        confirmVerified(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            observeLeft,
            observeRight,
            checkTextToSpeechAvailable,
            speakText,
        )
    }

    @Test
    fun `対象外のイベントは何も読み上げずカスタム文言を参照しない`() =
        runTest {
            speaker(SpeechEvent.KeepLeft, VOLUME)

            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言が空のときは本文を読み上げない`() =
        runTest {
            every { observeSectorYellow() } returns flowOf("")

            speaker(SpeechEvent.YellowFlag, VOLUME)
            verify(exactly = 1) { observeSectorYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言は設定されているがTTSが利用不可のときは本文を読み上げない`() =
        runTest {
            every { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.YellowFlag, VOLUME)
            verify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            every { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("イエロー、前方注意", volume = VOLUME) } just Runs

            speaker(SpeechEvent.YellowFlag, VOLUME)
            verify(exactly = 1) { observeSectorYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("イエロー、前方注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            every { observeBlue() } returns flowOf("ブルー、譲って")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("ブルー、譲って", volume = VOLUME) } just Runs

            speaker(SpeechEvent.BlueFlag, VOLUME)
            verify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("ブルー、譲って", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグのカスタム文言が空のときは読み上げない`() =
        runTest {
            every { observeBlue() } returns flowOf(" ")

            speaker(SpeechEvent.BlueFlag, VOLUME)
            verify(exactly = 1) { observeBlue() }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            every { observeFullCourseYellow() } returns flowOf("フルコースイエロー、減速")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("フルコースイエロー、減速", volume = VOLUME) } just Runs

            speaker(SpeechEvent.FullCourseYellow, VOLUME)
            verify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("フルコースイエロー、減速", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が空のときは読み上げない`() =
        runTest {
            every { observeFullCourseYellow() } returns flowOf("")

            speaker(SpeechEvent.FullCourseYellow, VOLUME)
            verify(exactly = 1) { observeFullCourseYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグの自由文字列を読み上げる`() =
        runTest {
            every { observeRed() } returns flowOf("赤旗、停止")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("赤旗、停止", volume = VOLUME) } just Runs

            speaker(SpeechEvent.RedFlag, VOLUME)

            verify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("赤旗、停止", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言が空のときは読み上げない`() =
        runTest {
            every { observeRed() } returns flowOf("")

            speaker(SpeechEvent.RedFlag, VOLUME)
            verify(exactly = 1) { observeRed() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言は設定されているがTTSが利用不可のときは読み上げない`() =
        runTest {
            every { observeRed() } returns flowOf("赤旗、停止")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.RedFlag, VOLUME)
            verify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローはTTSが利用不可なら本文を読み上げない`() =
        runTest {
            every { observeFullCourseYellow() } returns flowOf("フルコースイエロー")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.FullCourseYellow, VOLUME)

            verify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Leftの文言はTTSで読み上げる`() =
        runTest {
            every { observeLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("左注意", volume = VOLUME) } just Runs

            speaker(SpeechEvent.CarLeft, VOLUME)

            verify(exactly = 1) { observeLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("左注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `Leftの文言は空白なら読み上げない`() =
        runTest {
            every { observeLeft() } returns flowOf("  ")

            speaker(SpeechEvent.CarLeft, VOLUME)

            verify(exactly = 1) { observeLeft() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Leftの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.CarLeft, VOLUME)

            verify(exactly = 1) { observeLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言はTTSで読み上げる`() =
        runTest {
            every { observeRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("右注意", volume = VOLUME) } just Runs

            speaker(SpeechEvent.CarRight, VOLUME)

            verify(exactly = 1) { observeRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("右注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言は空白なら読み上げない`() =
        runTest {
            every { observeRight() } returns flowOf("  ")

            speaker(SpeechEvent.CarRight, VOLUME)

            verify(exactly = 1) { observeRight() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.CarRight, VOLUME)

            verify(exactly = 1) { observeRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    private companion object {
        const val VOLUME = 40
    }
}
