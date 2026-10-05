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
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
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
    private val observeRemainingText: ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase = mockk()
    private val observeBrakeText: ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase = mockk()
    private val observeTyreWearText: ObserveLmuWindowsTyreWearReadoutTextUseCase = mockk()
    private val observeTyreOverheatReadoutText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val observeTyreColdReadoutText: ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase = mockk()
    private val observeOverheatReadoutText: ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase = mockk()
    private val observePartDetachedReadoutText: ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase = mockk()
    private val observeTyreDetachedReadoutText: ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase = mockk()
    private val observeMyBestLapReadoutText: ObserveLmuWindowsMyBestLapReadoutTextUseCase = mockk()
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
            observeRemainingText,
            observeBrakeText,
            observeTyreWearText,
            observeTyreOverheatReadoutText,
            observeTyreColdReadoutText,
            observeOverheatReadoutText,
            observePartDetachedReadoutText,
            observeTyreDetachedReadoutText,
            observeMyBestLapReadoutText,
            checkTextToSpeechAvailable,
            speakText,
        )

    @Test
    fun `残量警告はイベントの設定閾値を文言に置換して読み上げる`() =
        runTest {
            every { observeRemainingText() } returns flowOf("閾値{percent}%、{percent}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("閾値50%、50", volume = VOLUME) } just Runs
            speaker(SpeechEvent.RemainingVirtualEnergyWarning(50), VOLUME)
            verify(exactly = 1) { observeRemainingText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("閾値50%、50", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `残量警告は解決済みの文言があれば設定を再取得せずその文言を読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            speaker(SpeechEvent.RemainingVirtualEnergyWarning(50, resolvedText = "解決済み"), VOLUME)
            verify(exactly = 0) { observeRemainingText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `残量警告の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeRemainingText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.RemainingVirtualEnergyWarning(30)))
            speaker(SpeechEvent.RemainingVirtualEnergyWarning(30), VOLUME)
            verify(exactly = 2) { observeRemainingText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `残量警告のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeRemainingText() } returns flowOf("残り{percent}%")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.RemainingVirtualEnergyWarning(70)))
            speaker(SpeechEvent.RemainingVirtualEnergyWarning(70), VOLUME)
            verify(exactly = 2) { observeRemainingText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("残り70%", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブレーキ過熱警告はイベントの設定閾値を文言に置換して読み上げる`() =
        runTest {
            every { observeBrakeText() } returns flowOf("閾値{celsius}℃、{celsius}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("閾値50℃、50", volume = VOLUME) } just Runs
            speaker(SpeechEvent.BrakeOverheat(50), VOLUME)
            verify(exactly = 1) { observeBrakeText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("閾値50℃、50", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブレーキ過熱警告は解決済みの文言があれば設定を再取得せずその文言を読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            speaker(SpeechEvent.BrakeOverheat(50, resolvedText = "解決済み"), VOLUME)
            verify(exactly = 0) { observeBrakeText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブレーキ過熱警告の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeBrakeText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.BrakeOverheat(30)))
            speaker(SpeechEvent.BrakeOverheat(30), VOLUME)
            verify(exactly = 2) { observeBrakeText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブレーキ過熱警告のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeBrakeText() } returns flowOf("残り{celsius}℃")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.BrakeOverheat(70)))
            speaker(SpeechEvent.BrakeOverheat(70), VOLUME)
            verify(exactly = 2) { observeBrakeText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("残り70℃", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブレーキ過熱警告の解決済み空白文言は既定文言へ戻さず読み上げない`() =
        runTest {
            val event = SpeechEvent.BrakeOverheat(50, resolvedText = " ")
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeBrakeText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `摩耗警告はイベントの設定閾値を文言に置換して読み上げる`() =
        runTest {
            every { observeTyreWearText() } returns flowOf("閾値{percent}%、{percent}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("閾値50%、50", volume = VOLUME) } just Runs
            speaker(SpeechEvent.TyreWearWarning(50), VOLUME)
            verify(exactly = 1) { observeTyreWearText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("閾値50%、50", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `摩耗警告は解決済みの文言があれば設定を再取得せずその文言を読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            speaker(SpeechEvent.TyreWearWarning(50, resolvedText = "解決済み"), VOLUME)
            verify(exactly = 0) { observeTyreWearText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `摩耗警告の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeTyreWearText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.TyreWearWarning(30)))
            speaker(SpeechEvent.TyreWearWarning(30), VOLUME)
            verify(exactly = 2) { observeTyreWearText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `摩耗警告のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeTyreWearText() } returns flowOf("残り{percent}%")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.TyreWearWarning(70)))
            speaker(SpeechEvent.TyreWearWarning(70), VOLUME)
            verify(exactly = 2) { observeTyreWearText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("残り70%", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `摩耗警告の解決済み空白文言は既定文言へ戻さず読み上げない`() =
        runTest {
            val event = SpeechEvent.TyreWearWarning(50, resolvedText = " ")
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeTyreWearText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告は保存した文言の温度を置換して読み上げる`() =
        runTest {
            every { observeTyreOverheatReadoutText() } returns flowOf("温度{celsius}℃、{unknown}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("温度100℃、{unknown}", volume = VOLUME) } just Runs
            speaker(SpeechEvent.TyreOverheat(100), VOLUME)
            verify(exactly = 1) { observeTyreOverheatReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("温度100℃、{unknown}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeTyreOverheatReadoutText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.TyreOverheat(100)))
            speaker(SpeechEvent.TyreOverheat(100), VOLUME)
            verify(exactly = 2) { observeTyreOverheatReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeTyreOverheatReadoutText() } returns flowOf("残り{percent}%")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.TyreOverheat(100)))
            speaker(SpeechEvent.TyreOverheat(100), VOLUME)
            verify(exactly = 2) { observeTyreOverheatReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("残り{percent}%", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告の読み上げ文言は保存値を返す`() =
        runTest {
            every { observeTyreOverheatReadoutText() } returns flowOf("タイヤを冷やして")
            coEvery { checkTextToSpeechAvailable() } returns true
            assertEquals("タイヤを冷やして", speaker.readoutText(SpeechEvent.TyreOverheat(100)))
            verify(exactly = 1) { observeTyreOverheatReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

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
            observeRemainingText,
            observeBrakeText,
            observeTyreWearText,
            observeTyreOverheatReadoutText,
            observeTyreColdReadoutText,
            observeOverheatReadoutText,
            observePartDetachedReadoutText,
            observeTyreDetachedReadoutText,
            observeMyBestLapReadoutText,
            checkTextToSpeechAvailable,
            speakText,
        )
    }

    @Test
    fun `対象外のイベントは何も読み上げずカスタム文言を参照しない`() =
        runTest {
            speaker(SpeechEvent.Gt7Ps5MyBestLap(lapTimeMs = 83_456), VOLUME)

            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言が空のときは本文を読み上げない`() =
        runTest {
            every { observeSectorYellow() } returns flowOf("")

            speaker(SpeechEvent.YellowFlag(), VOLUME)
            verify(exactly = 1) { observeSectorYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `セクターイエローのカスタム文言は設定されているがTTSが利用不可のときは本文を読み上げない`() =
        runTest {
            every { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.YellowFlag(), VOLUME)
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

            speaker(SpeechEvent.YellowFlag(), VOLUME)
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

            speaker(SpeechEvent.BlueFlag(), VOLUME)
            verify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("ブルー、譲って", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブルーフラッグのカスタム文言が空のときは読み上げない`() =
        runTest {
            every { observeBlue() } returns flowOf(" ")

            speaker(SpeechEvent.BlueFlag(), VOLUME)
            verify(exactly = 1) { observeBlue() }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            every { observeFullCourseYellow() } returns flowOf("フルコースイエロー、減速")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("フルコースイエロー、減速", volume = VOLUME) } just Runs

            speaker(SpeechEvent.FullCourseYellow(), VOLUME)
            verify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("フルコースイエロー、減速", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が空のときは読み上げない`() =
        runTest {
            every { observeFullCourseYellow() } returns flowOf("")

            speaker(SpeechEvent.FullCourseYellow(), VOLUME)
            verify(exactly = 1) { observeFullCourseYellow() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグの自由文字列を読み上げる`() =
        runTest {
            every { observeRed() } returns flowOf("赤旗、停止")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("赤旗、停止", volume = VOLUME) } just Runs

            speaker(SpeechEvent.RedFlag(), VOLUME)

            verify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("赤旗、停止", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言が空のときは読み上げない`() =
        runTest {
            every { observeRed() } returns flowOf("")

            speaker(SpeechEvent.RedFlag(), VOLUME)
            verify(exactly = 1) { observeRed() }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグのカスタム文言は設定されているがTTSが利用不可のときは読み上げない`() =
        runTest {
            every { observeRed() } returns flowOf("赤旗、停止")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.RedFlag(), VOLUME)
            verify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローはTTSが利用不可なら本文を読み上げない`() =
        runTest {
            every { observeFullCourseYellow() } returns flowOf("フルコースイエロー")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.FullCourseYellow(), VOLUME)

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
            val events = listOf(SpeechEvent.Gt7Ps5MyBestLap(lapTimeMs = 83_456))
            events.forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, VOLUME)
            }
            confirmAllMocksVerified()
        }

    @Test
    fun `低温警告は保存した文言の温度を置換して読み上げる`() =
        runTest {
            every { observeTyreColdReadoutText() } returns flowOf("温度{celsius}℃、{unknown}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("温度60℃、{unknown}", volume = VOLUME) } just Runs
            speaker(SpeechEvent.TyreCold(60), VOLUME)
            verify(exactly = 1) { observeTyreColdReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("温度60℃、{unknown}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `低温警告の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeTyreColdReadoutText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.TyreCold(60)))
            speaker(SpeechEvent.TyreCold(60), VOLUME)
            verify(exactly = 2) { observeTyreColdReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `低温警告のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeTyreColdReadoutText() } returns flowOf("残り{percent}%")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.TyreCold(60)))
            speaker(SpeechEvent.TyreCold(60), VOLUME)
            verify(exactly = 2) { observeTyreColdReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("残り{percent}%", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `低温警告の読み上げ文言は保存値を返す`() =
        runTest {
            every { observeTyreColdReadoutText() } returns flowOf("タイヤを温めて")
            coEvery { checkTextToSpeechAvailable() } returns true
            assertEquals("タイヤを温めて", speaker.readoutText(SpeechEvent.TyreCold(60)))
            verify(exactly = 1) { observeTyreColdReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱と低温はそれぞれの保存文言を読み上げる`() =
        runTest {
            every { observeTyreOverheatReadoutText() } returns flowOf("冷やして")
            every { observeTyreColdReadoutText() } returns flowOf("温めて{celsius}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("冷やして", volume = VOLUME) } just Runs
            coEvery { speakText("温めて60", volume = VOLUME) } just Runs
            speaker(SpeechEvent.TyreOverheat(100), VOLUME)
            speaker(SpeechEvent.TyreCold(60), VOLUME)
            verify(exactly = 1) { observeTyreOverheatReadoutText() }
            verify(exactly = 1) { observeTyreColdReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("冷やして", volume = VOLUME) }
            coVerify(exactly = 1) { speakText("温めて60", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告は解決済み文言を保存値より優先する`() =
        runTest {
            val event = SpeechEvent.TyreOverheat(100, resolvedText = "判定時{celsius}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時{celsius}", volume = VOLUME) } just Runs
            assertEquals("判定時{celsius}", speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeTyreOverheatReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("判定時{celsius}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `低温警告は解決済み文言を保存値より優先する`() =
        runTest {
            val event = SpeechEvent.TyreCold(60, resolvedText = "判定時{celsius}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時{celsius}", volume = VOLUME) } just Runs
            assertEquals("判定時{celsius}", speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeTyreColdReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("判定時{celsius}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    private companion object {
        const val VOLUME = 40
    }

    @Test
    fun `オーバーヒートは保存した自由文言をそのまま読み上げる`() =
        runTest {
            every { observeOverheatReadoutText() } returns flowOf("自由文言{literal}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("自由文言{literal}", volume = VOLUME) } just Runs
            speaker(SpeechEvent.Overheating(), VOLUME)
            verify(exactly = 1) { observeOverheatReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("自由文言{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `オーバーヒートは解決済みの文言があれば設定を再取得せずその文言を読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            speaker(SpeechEvent.Overheating(resolvedText = "解決済み"), VOLUME)
            verify(exactly = 0) { observeOverheatReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `オーバーヒートの空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeOverheatReadoutText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.Overheating()))
            speaker(SpeechEvent.Overheating(), VOLUME)
            verify(exactly = 2) { observeOverheatReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `オーバーヒートのTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeOverheatReadoutText() } returns flowOf("自由文言")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.Overheating()))
            speaker(SpeechEvent.Overheating(), VOLUME)
            verify(exactly = 2) { observeOverheatReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("自由文言", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `オーバーヒートの解決済み空白文言は既定文言へ戻さず読み上げない`() =
        runTest {
            val event = SpeechEvent.Overheating(resolvedText = " ")
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeOverheatReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `自己ベストラップは保存した文言のタイムを置換して読み上げる`() =
        runTest {
            every { observeMyBestLapReadoutText() } returns flowOf("更新{laptime}{literal}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("更新1分23秒456{literal}", volume = VOLUME) } just Runs
            speaker(SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L), VOLUME)
            verify(exactly = 1) { observeMyBestLapReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("更新1分23秒456{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `自己ベストラップは解決済みの文言があれば設定を再取得せずその文言を読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            speaker(SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L, resolvedText = "解決済み"), VOLUME)
            verify(exactly = 0) { observeMyBestLapReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `自己ベストラップの空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeMyBestLapReadoutText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L)))
            speaker(SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L), VOLUME)
            verify(exactly = 2) { observeMyBestLapReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `自己ベストラップのTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeMyBestLapReadoutText() } returns flowOf("自由文言")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L)))
            speaker(SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L), VOLUME)
            verify(exactly = 2) { observeMyBestLapReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("自由文言", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `自己ベストラップの解決済み空白文言は既定文言へ戻さず読み上げない`() =
        runTest {
            val event = SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L, resolvedText = " ")
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeMyBestLapReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `部品脱落は保存した自由文言をそのまま読み上げる`() =
        runTest {
            every { observePartDetachedReadoutText() } returns flowOf("自由文言{literal}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("自由文言{literal}", volume = VOLUME) } just Runs
            speaker(SpeechEvent.PartDetached(), VOLUME)
            verify(exactly = 1) { observePartDetachedReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("自由文言{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `部品脱落は解決済みの文言があれば設定を再取得せずその文言を読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            speaker(SpeechEvent.PartDetached(resolvedText = "解決済み"), VOLUME)
            verify(exactly = 0) { observePartDetachedReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `部品脱落の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observePartDetachedReadoutText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.PartDetached()))
            speaker(SpeechEvent.PartDetached(), VOLUME)
            verify(exactly = 2) { observePartDetachedReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `部品脱落のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observePartDetachedReadoutText() } returns flowOf("自由文言")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.PartDetached()))
            speaker(SpeechEvent.PartDetached(), VOLUME)
            verify(exactly = 2) { observePartDetachedReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("自由文言", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `部品脱落の解決済み空白文言は既定文言へ戻さず読み上げない`() =
        runTest {
            val event = SpeechEvent.PartDetached(resolvedText = " ")
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observePartDetachedReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ脱落は保存した自由文言をそのまま読み上げる`() =
        runTest {
            every { observeTyreDetachedReadoutText() } returns flowOf("自由文言{literal}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("自由文言{literal}", volume = VOLUME) } just Runs
            speaker(SpeechEvent.TyreDetached(), VOLUME)
            verify(exactly = 1) { observeTyreDetachedReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("自由文言{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ脱落は解決済みの文言があれば設定を再取得せずその文言を読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            speaker(SpeechEvent.TyreDetached(resolvedText = "解決済み"), VOLUME)
            verify(exactly = 0) { observeTyreDetachedReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ脱落の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeTyreDetachedReadoutText() } returns flowOf(" ")
            assertNull(speaker.readoutText(SpeechEvent.TyreDetached()))
            speaker(SpeechEvent.TyreDetached(), VOLUME)
            verify(exactly = 2) { observeTyreDetachedReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ脱落のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeTyreDetachedReadoutText() } returns flowOf("自由文言")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.TyreDetached()))
            speaker(SpeechEvent.TyreDetached(), VOLUME)
            verify(exactly = 2) { observeTyreDetachedReadoutText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("自由文言", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ脱落の解決済み空白文言は既定文言へ戻さず読み上げない`() =
        runTest {
            val event = SpeechEvent.TyreDetached(resolvedText = " ")
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeTyreDetachedReadoutText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }
}
