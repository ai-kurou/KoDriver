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
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearLow
import kurou.kodriver.domain.engine.LmuWindowsFullCourseYellow
import kurou.kodriver.domain.engine.LmuWindowsManualReadoutText
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.engine.LmuWindowsYellowFlag
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class LmuWindowsReadoutTextSpeakerTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

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
    private val observePitTimingFuelReadoutText: ObserveLmuWindowsPitTimingFuelReadoutTextUseCase = mockk()
    private val observePitTimingTyreWearImminentReadoutText:
        ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase = mockk()

    private val observePitTimingFuelImminentReadoutText:
        ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCase = mockk()
    private val observeRemainingText: ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase = mockk()
    private val observeRemainingFuelText: ObserveLmuWindowsRemainingFuelReadoutTextUseCase = mockk()
    private val observeBrakeText: ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase = mockk()
    private val observeBrakeWearText: ObserveLmuWindowsBrakeWearReadoutTextUseCase = mockk()
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
            readoutTexts =
                LmuWindowsReadoutTextUseCases(
                    flag =
                        LmuWindowsFlagReadoutTextUseCases(
                            sectorYellow = observeSectorYellow,
                            blue = observeBlue,
                            fullCourseYellow = observeFullCourseYellow,
                            red = observeRed,
                        ),
                    vehicleApproach =
                        LmuWindowsVehicleApproachReadoutTextUseCases(
                            startLeft = observeLeft,
                            startRight = observeRight,
                            sustainedLeft = observeSustainedLeft,
                            sustainedRight = observeSustainedRight,
                        ),
                    pitTiming =
                        LmuWindowsPitTimingReadoutTextUseCases(
                            virtualEnergy = observePitTimingVirtualEnergyReadoutText,
                            virtualEnergyImminent = observePitTimingVirtualEnergyImminentReadoutText,
                            tyreWear = observePitTimingTyreWearReadoutText,
                            fuel = observePitTimingFuelReadoutText,
                            tyreWearImminent = observePitTimingTyreWearImminentReadoutText,
                            fuelImminent = observePitTimingFuelImminentReadoutText,
                        ),
                    tyre =
                        LmuWindowsTyreReadoutTextUseCases(
                            wear = observeTyreWearText,
                            overheat = observeTyreOverheatReadoutText,
                            cold = observeTyreColdReadoutText,
                        ),
                    vehicleDamage =
                        LmuWindowsVehicleDamageReadoutTextUseCases(
                            overheat = observeOverheatReadoutText,
                            partDetached = observePartDetachedReadoutText,
                            tyreDetached = observeTyreDetachedReadoutText,
                        ),
                    brake =
                        LmuWindowsBrakeReadoutTextUseCases(
                            temperature = observeBrakeText,
                            wear = observeBrakeWearText,
                        ),
                    remainingVirtualEnergy = observeRemainingText,
                    remainingFuel = observeRemainingFuelText,
                    myBestLap = observeMyBestLapReadoutText,
                ),
            checkTextToSpeechAvailable = checkTextToSpeechAvailable,
            speakText = speakText,
        )

    @Test
    fun `現在のブレーキ残量は既定文言と解決済み文言を読む`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            listOf(LmuWindowsBrakeWearCurrentValue(42), LmuWindowsBrakeWearCurrentValue(42, "残り42%")).forEach { event ->
                coEvery { speakText(event.narratedText, volume = VOLUME) } just Runs
                speaker(event, VOLUME)
                coVerify(exactly = 1) { speakText(event.narratedText, volume = VOLUME) }
            }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `手動セットは連結本文を一回だけ読み上げる`() =
        runTest {
            val text = "VE50%、タイヤ80%"
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText(text, volume = VOLUME) } just Runs
            speaker(LmuWindowsManualReadoutText(LmuWindowsReadoutItemKey.TyreWear.Root, text), VOLUME)
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText(text, volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `手動セットは解決済み本文を読み設定を取得しない`() =
        runTest {
            val event = LmuWindowsManualReadoutText(LmuWindowsReadoutItemKey.TyreWear.Root, "元の文", "解決済み")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("解決済み", volume = VOLUME) } just Runs
            assertEquals("解決済み", speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeTyreWearText() }
            verify(exactly = 0) { observeRemainingText() }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("解決済み", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `手動セットは未解決と解決済みの空文字や空白を読み上げない`() =
        runTest {
            val key = LmuWindowsReadoutItemKey.TyreWear.Root
            listOf("", " ").forEach { text ->
                listOf(
                    LmuWindowsManualReadoutText(key, text),
                    LmuWindowsManualReadoutText(key, "元の文", text),
                ).forEach { event ->
                    assertNull(speaker.readoutText(event))
                    speaker(event, VOLUME)
                }
            }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText("", volume = VOLUME) }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `自由文言対象はデバッグ要求とLMUイベントだけ`() {
        val key = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).first()
        assertTrue(isLmuWindowsCustomSpeakEvent(DebugReadoutText(key, "入力文言")))
        assertTrue(isLmuWindowsCustomSpeakEvent(LmuWindowsBlueFlag()))
        assertFalse(isLmuWindowsCustomSpeakEvent(ForeignReadoutTextEvent()))
    }

    @Test
    fun `他シミュレーターのイベントは解決済み本文があっても読み上げない`() =
        runTest {
            val text = "他シミュレーターの本文"
            val events =
                listOf(
                    ForeignReadoutTextEvent(),
                    ForeignReadoutTextEvent(resolvedText = text),
                )
            events.forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, VOLUME)
            }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(text, volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `現在のエネルギー残量は警告設定を取得せず既定文言で読み上げる`() =
        runTest {
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("バーチャルエネルギー残量は42パーセント", volume = VOLUME) } just Runs
            speaker(LmuWindowsRemainingVirtualEnergyCurrentValue(42), VOLUME)
            verify(exactly = 0) { observeRemainingText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("バーチャルエネルギー残量は42パーセント", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `現在のエネルギー残量の解決済み本文が空白ならTTSを確認せず読み上げない`() =
        runTest {
            val event = LmuWindowsRemainingVirtualEnergyCurrentValue(42, resolvedText = " ")
            assertNull(speaker.readoutText(event))
            speaker(event, VOLUME)
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `残量警告はイベントの設定閾値を文言に置換して読み上げる`() =
        runTest {
            every { observeRemainingText() } returns flowOf("閾値{percent}%、{percent}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("閾値50%、50", volume = VOLUME) } just Runs
            speaker(LmuWindowsRemainingVirtualEnergyWarning(50), VOLUME)
            verify(exactly = 1) { observeRemainingText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("閾値50%、50", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `残量警告の空白文言ではTTSを確認せず読み上げない`() =
        runTest {
            every { observeRemainingText() } returns flowOf(" ")
            assertNull(speaker.readoutText(LmuWindowsRemainingVirtualEnergyWarning(30)))
            speaker(LmuWindowsRemainingVirtualEnergyWarning(30), VOLUME)
            verify(exactly = 2) { observeRemainingText() }
            coVerify(exactly = 0) { checkTextToSpeechAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `燃料残量警告は閾値を文言に置換して読み上げる`() =
        runTest {
            every { observeRemainingFuelText() } returns flowOf("燃料{percent}%以下")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("燃料20%以下", volume = VOLUME) } just Runs
            speaker(LmuWindowsRemainingFuelWarning(20), VOLUME)
            verify(exactly = 1) { observeRemainingFuelText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("燃料20%以下", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `残量警告のTTS利用不可では読み上げ文言を返さず読み上げない`() =
        runTest {
            every { observeRemainingText() } returns flowOf("残り{percent}%")
            coEvery { checkTextToSpeechAvailable() } returns false
            assertNull(speaker.readoutText(LmuWindowsRemainingVirtualEnergyWarning(70)))
            speaker(LmuWindowsRemainingVirtualEnergyWarning(70), VOLUME)
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
            speaker(LmuWindowsBrakeOverheat(50), VOLUME)
            verify(exactly = 1) { observeBrakeText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("閾値50℃、50", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `ブレーキ残量警告はイベントの設定閾値を文言に置換して読み上げる`() =
        runTest {
            every { observeBrakeWearText() } returns flowOf("閾値{percent}%、{percent}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("閾値50%、50", volume = VOLUME) } just Runs
            speaker(LmuWindowsBrakeWearLow(50), VOLUME)
            verify(exactly = 1) { observeBrakeWearText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("閾値50%、50", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `摩耗警告はイベントの設定閾値を文言に置換して読み上げる`() =
        runTest {
            every { observeTyreWearText() } returns flowOf("閾値{percent}%、{percent}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("閾値50%、50", volume = VOLUME) } just Runs
            speaker(LmuWindowsTyreWearWarning(50), VOLUME)
            verify(exactly = 1) { observeTyreWearText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("閾値50%、50", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告は保存した文言の温度を置換して読み上げる`() =
        runTest {
            every { observeTyreOverheatReadoutText() } returns flowOf("温度{celsius}℃、{unknown}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("温度100℃、{unknown}", volume = VOLUME) } just Runs
            speaker(LmuWindowsTyreOverheat(100), VOLUME)
            verify(exactly = 1) { observeTyreOverheatReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("温度100℃、{unknown}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告の読み上げ文言は保存値を返す`() =
        runTest {
            every { observeTyreOverheatReadoutText() } returns flowOf("タイヤを冷やして")
            coEvery { checkTextToSpeechAvailable() } returns true
            assertEquals("タイヤを冷やして", speaker.readoutText(LmuWindowsTyreOverheat(100)))
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
            observePitTimingFuelReadoutText,
            observePitTimingTyreWearImminentReadoutText,
            observePitTimingFuelImminentReadoutText,
            observeRemainingText,
            observeRemainingFuelText,
            observeBrakeText,
            observeBrakeWearText,
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
    fun `セクターイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            every { observeSectorYellow() } returns flowOf("イエロー、前方注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("イエロー、前方注意", volume = VOLUME) } just Runs

            speaker(LmuWindowsYellowFlag(), VOLUME)
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

            speaker(LmuWindowsBlueFlag(), VOLUME)
            verify(exactly = 1) { observeBlue() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("ブルー、譲って", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `フルコースイエローのカスタム文言が設定されておりTTSが利用可能なときはOS標準TTSで読み上げる`() =
        runTest {
            every { observeFullCourseYellow() } returns flowOf("フルコースイエロー、減速")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("フルコースイエロー、減速", volume = VOLUME) } just Runs

            speaker(LmuWindowsFullCourseYellow(), VOLUME)
            verify(exactly = 1) { observeFullCourseYellow() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("フルコースイエロー、減速", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `レッドフラッグの自由文字列を読み上げる`() =
        runTest {
            every { observeRed() } returns flowOf("赤旗、停止")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("赤旗、停止", volume = VOLUME) } just Runs

            speaker(LmuWindowsRedFlag(), VOLUME)

            verify(exactly = 1) { observeRed() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("赤旗、停止", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `VE残り1周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingVirtualEnergyReadoutText() } returns flowOf("残り{laps}周")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("残り1周", volume = VOLUME) } just Runs

            speaker(LmuWindowsPitTimingWarning(1, PitTimingSource.VirtualEnergy), VOLUME)

            verify(exactly = 1) { observePitTimingVirtualEnergyReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("残り1周", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `VE残り0周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingVirtualEnergyImminentReadoutText() } returns flowOf("必ず{laps}ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("必ず{laps}ピットイン", volume = VOLUME) } just Runs

            speaker(LmuWindowsPitTimingWarning(0, PitTimingSource.VirtualEnergy), VOLUME)

            verify(exactly = 1) { observePitTimingVirtualEnergyImminentReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("必ず{laps}ピットイン", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ摩耗残り1周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingTyreWearReadoutText() } returns flowOf("残り{laps}周")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("残り1周", volume = VOLUME) } just Runs

            speaker(LmuWindowsPitTimingWarning(1, PitTimingSource.TyreWear), VOLUME)

            verify(exactly = 1) { observePitTimingTyreWearReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("残り1周", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ摩耗残り0周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingTyreWearImminentReadoutText() } returns flowOf("必ず{laps}ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("必ず{laps}ピットイン", volume = VOLUME) } just Runs

            speaker(LmuWindowsPitTimingWarning(0, PitTimingSource.TyreWear), VOLUME)

            verify(exactly = 1) { observePitTimingTyreWearImminentReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("必ず{laps}ピットイン", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `対象外イベントの文言はnullで読み上げない`() =
        runTest {
            val events = listOf(ForeignReadoutTextEvent())
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
            speaker(LmuWindowsTyreCold(60), VOLUME)
            verify(exactly = 1) { observeTyreColdReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("温度60℃、{unknown}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `過熱警告は解決済み文言を保存値より優先する`() =
        runTest {
            val event = LmuWindowsTyreOverheat(100, resolvedText = "判定時{celsius}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時{celsius}", volume = VOLUME) } just Runs
            assertEquals("判定時{celsius}", speaker.readoutText(event))
            speaker(event, VOLUME)
            verify(exactly = 0) { observeTyreOverheatReadoutText() }
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
            speaker(LmuWindowsOverheating(), VOLUME)
            verify(exactly = 1) { observeOverheatReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("自由文言{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `自己ベストラップは保存した文言のタイムを置換して読み上げる`() =
        runTest {
            every { observeMyBestLapReadoutText() } returns flowOf("更新{laptime}{literal}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("更新1分23秒456{literal}", volume = VOLUME) } just Runs
            speaker(LmuWindowsMyBestLap(lapTimeMs = 83_456L), VOLUME)
            verify(exactly = 1) { observeMyBestLapReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("更新1分23秒456{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `部品脱落は保存した自由文言をそのまま読み上げる`() =
        runTest {
            every { observePartDetachedReadoutText() } returns flowOf("自由文言{literal}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("自由文言{literal}", volume = VOLUME) } just Runs
            speaker(LmuWindowsPartDetached(), VOLUME)
            verify(exactly = 1) { observePartDetachedReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("自由文言{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `タイヤ脱落は保存した自由文言をそのまま読み上げる`() =
        runTest {
            every { observeTyreDetachedReadoutText() } returns flowOf("自由文言{literal}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("自由文言{literal}", volume = VOLUME) } just Runs
            speaker(LmuWindowsTyreDetached(), VOLUME)
            verify(exactly = 1) { observeTyreDetachedReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("自由文言{literal}", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `デバッグ要求の本文はテンプレート置換せずTTSで再生する`() =
        runTest {
            val key = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).first()
            val event = DebugReadoutText(key, "入力 {laps}")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText(event.resolvedText, volume = 42) } just Runs

            assertEquals(event.resolvedText, speaker.readoutText(event))
            speaker(event, 42)
            assertNull(speaker.readoutText(event.copy(resolvedText = " ")))
            speaker(event.copy(resolvedText = " "), 42)
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText(event.resolvedText, volume = 42) }
            confirmVerified(checkTextToSpeechAvailable, speakText)
        }

    @Test
    fun `燃料残り1周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingFuelReadoutText() } returns flowOf("残り{laps}周")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("残り1周", volume = VOLUME) } just Runs

            speaker(LmuWindowsPitTimingWarning(1, PitTimingSource.Fuel), VOLUME)

            verify(exactly = 1) { observePitTimingFuelReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("残り1周", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `燃料残り0周は対応する文言を音量付きで読み上げる`() =
        runTest {
            every { observePitTimingFuelImminentReadoutText() } returns flowOf("必ず{laps}ピットイン")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("必ず{laps}ピットイン", volume = VOLUME) } just Runs

            speaker(LmuWindowsPitTimingWarning(0, PitTimingSource.Fuel), VOLUME)

            verify(exactly = 1) { observePitTimingFuelImminentReadoutText() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("必ず{laps}ピットイン", volume = VOLUME) }
            confirmAllMocksVerified()
        }
}

/** 他シミュレーターのイベントを表すテスト用ダブル。他モジュールのイベント型には依存できないため、共通の [ReadoutTextEvent] で代用する。 */
private data class ForeignReadoutTextEvent(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey: ReadoutItemKey = ReadoutItemKey.entries.first()
    override val narratedText: String = "他シミュレーターのイベント"

    override fun withResolvedText(text: String): ForeignReadoutTextEvent = copy(resolvedText = text)
}
