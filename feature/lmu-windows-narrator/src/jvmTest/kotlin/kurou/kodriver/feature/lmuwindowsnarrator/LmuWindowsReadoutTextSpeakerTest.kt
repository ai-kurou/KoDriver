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
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("TooManyFunctions")
class LmuWindowsReadoutTextSpeakerTest {
    private val observeSectorYellow: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase = mockk()
    private val observeBlue: ObserveLmuWindowsBlueFlagReadoutTextUseCase = mockk()
    private val observeFullCourseYellow: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase = mockk()
    private val observeRed: ObserveLmuWindowsRedFlagReadoutTextUseCase = mockk()
    private val observeLeft: ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase = mockk()
    private val observeRight: ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase = mockk()
    private val observeSustainedLeft: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase = mockk()
    private val observeSustainedRight: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase = mockk()
    private val observePitTimingVirtualEnergyReadoutText: ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase =
        mockk()
    private val observePitTimingVirtualEnergyImminentReadoutText:
        ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase = mockk()
    private val observePitTimingTyreWearReadoutText: ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase = mockk()
    private val observePitTimingTyreWearImminentReadoutText:
        ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase = mockk()
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
            observeSustainedLeft,
            observeSustainedRight,
            observePitTimingVirtualEnergyReadoutText,
            observePitTimingVirtualEnergyImminentReadoutText,
            observePitTimingTyreWearReadoutText,
            observePitTimingTyreWearImminentReadoutText,
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
            observeSustainedLeft,
            observeSustainedRight,
            observePitTimingVirtualEnergyReadoutText,
            observePitTimingVirtualEnergyImminentReadoutText,
            observePitTimingTyreWearReadoutText,
            observePitTimingTyreWearImminentReadoutText,
            checkTextToSpeechAvailable,
            speakText,
        )
    }

    @Test
    fun `対象外のイベントは何も読み上げずカスタム文言を参照しない`() =
        runTest {
            speaker(SpeechEvent.Overheating, VOLUME)

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
    fun `VE残り1周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingVirtualEnergyReadoutText() } returns flowOf("残り{laps}周")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("残り1周", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(1, PitTimingSource.VirtualEnergy), VOLUME)

            verify(exactly = 1) { observePitTimingVirtualEnergyReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("残り1周", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `VE残り5周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingVirtualEnergyReadoutText() } returns flowOf("残り{laps}周")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("残り5周", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(5, PitTimingSource.VirtualEnergy), VOLUME)

            verify(exactly = 1) { observePitTimingVirtualEnergyReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("残り5周", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `VirtualEnergyReadoutTextは空白文言なら読み上げない`() =
        runTest {
            every { observePitTimingVirtualEnergyReadoutText() } returns flowOf("  ")

            val event = SpeechEvent.PitTimingWarning(1, PitTimingSource.VirtualEnergy)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingVirtualEnergyReadoutText() }
            confirmAllMocksVerified()
        }

    @Test
    fun `VirtualEnergyReadoutTextはTTS利用不可なら読み上げない`() =
        runTest {
            every { observePitTimingVirtualEnergyReadoutText() } returns flowOf("ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns false

            val event = SpeechEvent.PitTimingWarning(1, PitTimingSource.VirtualEnergy)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingVirtualEnergyReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `VE残り0周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingVirtualEnergyImminentReadoutText() } returns flowOf("必ず{laps}ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("必ず{laps}ピットイン", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(0, PitTimingSource.VirtualEnergy), VOLUME)

            verify(exactly = 1) { observePitTimingVirtualEnergyImminentReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("必ず{laps}ピットイン", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `VE残り-1周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingVirtualEnergyImminentReadoutText() } returns flowOf("必ず{laps}ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("必ず{laps}ピットイン", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(-1, PitTimingSource.VirtualEnergy), VOLUME)

            verify(exactly = 1) { observePitTimingVirtualEnergyImminentReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("必ず{laps}ピットイン", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `VirtualEnergyImminentReadoutTextは空白文言なら読み上げない`() =
        runTest {
            every { observePitTimingVirtualEnergyImminentReadoutText() } returns flowOf("  ")

            val event = SpeechEvent.PitTimingWarning(0, PitTimingSource.VirtualEnergy)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingVirtualEnergyImminentReadoutText() }
            confirmAllMocksVerified()
        }

    @Test
    fun `VirtualEnergyImminentReadoutTextはTTS利用不可なら読み上げない`() =
        runTest {
            every { observePitTimingVirtualEnergyImminentReadoutText() } returns flowOf("ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns false

            val event = SpeechEvent.PitTimingWarning(0, PitTimingSource.VirtualEnergy)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingVirtualEnergyImminentReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ摩耗残り1周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingTyreWearReadoutText() } returns flowOf("残り{laps}周")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("残り1周", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(1, PitTimingSource.TyreWear), VOLUME)

            verify(exactly = 1) { observePitTimingTyreWearReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("残り1周", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ摩耗残り5周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingTyreWearReadoutText() } returns flowOf("残り{laps}周")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("残り5周", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(5, PitTimingSource.TyreWear), VOLUME)

            verify(exactly = 1) { observePitTimingTyreWearReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("残り5周", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `TyreWearReadoutTextは空白文言なら読み上げない`() =
        runTest {
            every { observePitTimingTyreWearReadoutText() } returns flowOf("  ")

            val event = SpeechEvent.PitTimingWarning(1, PitTimingSource.TyreWear)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingTyreWearReadoutText() }
            confirmAllMocksVerified()
        }

    @Test
    fun `TyreWearReadoutTextはTTS利用不可なら読み上げない`() =
        runTest {
            every { observePitTimingTyreWearReadoutText() } returns flowOf("ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns false

            val event = SpeechEvent.PitTimingWarning(1, PitTimingSource.TyreWear)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingTyreWearReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ摩耗残り0周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingTyreWearImminentReadoutText() } returns flowOf("必ず{laps}ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("必ず{laps}ピットイン", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(0, PitTimingSource.TyreWear), VOLUME)

            verify(exactly = 1) { observePitTimingTyreWearImminentReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("必ず{laps}ピットイン", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ摩耗残り-1周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingTyreWearImminentReadoutText() } returns flowOf("必ず{laps}ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("必ず{laps}ピットイン", volume = VOLUME) } just Runs

            speaker(SpeechEvent.PitTimingWarning(-1, PitTimingSource.TyreWear), VOLUME)

            verify(exactly = 1) { observePitTimingTyreWearImminentReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("必ず{laps}ピットイン", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `TyreWearImminentReadoutTextは空白文言なら読み上げない`() =
        runTest {
            every { observePitTimingTyreWearImminentReadoutText() } returns flowOf("  ")

            val event = SpeechEvent.PitTimingWarning(0, PitTimingSource.TyreWear)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingTyreWearImminentReadoutText() }
            confirmAllMocksVerified()
        }

    @Test
    fun `TyreWearImminentReadoutTextはTTS利用不可なら読み上げない`() =
        runTest {
            every { observePitTimingTyreWearImminentReadoutText() } returns flowOf("ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns false

            val event = SpeechEvent.PitTimingWarning(0, PitTimingSource.TyreWear)
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)

            verify(exactly = 2) { observePitTimingTyreWearImminentReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `対象外イベントの文言はnullで読み上げない`() =
        runTest {
            val events = listOf(SpeechEvent.Overheating)
            events.forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, VOLUME)
            }
            confirmAllMocksVerified()
        }

    private companion object {
        const val VOLUME = 40
    }
}
