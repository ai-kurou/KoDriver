@file:Suppress("TooManyFunctions")

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
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.engine.AceWindowsBlackFlag
import kurou.kodriver.domain.engine.AceWindowsBlackWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsBlueFlag
import kurou.kodriver.domain.engine.AceWindowsCheckeredFlag
import kurou.kodriver.domain.engine.AceWindowsGreenFlag
import kurou.kodriver.domain.engine.AceWindowsManualReadoutText
import kurou.kodriver.domain.engine.AceWindowsMyBestLap
import kurou.kodriver.domain.engine.AceWindowsMyBestLapCurrentValue
import kurou.kodriver.domain.engine.AceWindowsOrangeCircleFlag
import kurou.kodriver.domain.engine.AceWindowsRedFlag
import kurou.kodriver.domain.engine.AceWindowsRedYellowStripesFlag
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelCurrentValue
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsCurrentValue
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsWarning
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.AceWindowsTyreOverheat
import kurou.kodriver.domain.engine.AceWindowsTyreTemperatureCurrentValue
import kurou.kodriver.domain.engine.AceWindowsVehicleApproach
import kurou.kodriver.domain.engine.AceWindowsWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsYellowFlag
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AceWindowsReadoutTextSpeakerTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
    }

    private val observeFlagText: ObserveAceWindowsFlagReadoutTextUseCase = mockk()
    private val observeTyreOverheat: ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val observeVehicleApproach: ObserveAceWindowsVehicleApproachReadoutTextUseCase = mockk()
    private val observeRemainingFuel: ObserveAceWindowsRemainingFuelReadoutTextUseCase = mockk()
    private val observeRemainingFuelLaps: ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase = mockk()
    private val observeEmptyFuelLaps: ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase = mockk()
    private val observeMyBestLap: ObserveAceWindowsMyBestLapReadoutTextUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        AceWindowsReadoutTextSpeaker(
            readoutTexts =
                AceWindowsReadoutTextUseCases(
                    flag = observeFlagText,
                    remainingFuel =
                        AceWindowsRemainingFuelReadoutTextUseCases(
                            fuel = observeRemainingFuel,
                            fuelLaps = observeRemainingFuelLaps,
                            fuelLapsEmpty = observeEmptyFuelLaps,
                        ),
                    vehicleApproach = observeVehicleApproach,
                    tyreOverheat = observeTyreOverheat,
                    myBestLap = observeMyBestLap,
                ),
            checkTextToSpeechAvailable = checkAvailable,
            speakText = speakText,
        )

    @Test
    fun `手動燃料周回数の本文と解決済み文言を再生する`() =
        runTest {
            val event = AceWindowsRemainingFuelLapsCurrentValue(5)
            coEvery { checkAvailable() } returns true
            coEvery { speakText(event.narratedText, volume = 80) } just Runs
            coEvery { speakText("解決済み", volume = 80) } just Runs

            assertEquals(event.narratedText, speaker.readoutText(event))
            speaker(event, 80)
            val resolved = event.withResolvedText("解決済み")
            assertEquals("解決済み", speaker.readoutText(resolved))
            speaker(resolved, 80)
            assertNull(speaker.readoutText(event.withResolvedText(" ")))
            speaker(event.withResolvedText(" "), 80)

            coVerify(exactly = 3) { checkAvailable() }
            coVerify(exactly = 1) { speakText(event.narratedText, volume = 80) }
            coVerify(exactly = 1) { speakText("解決済み", volume = 80) }
            confirmVerified(checkAvailable, speakText)
        }

    @Test
    fun `手動タイヤ温度本文を再生する`() =
        runTest {
            val event = AceWindowsTyreTemperatureCurrentValue(90)
            coEvery { checkAvailable() } returns true
            coEvery { speakText(event.narratedText, volume = 80) } just Runs
            assertEquals(event.narratedText, speaker.readoutText(event))
            speaker(event, 80)
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText(event.narratedText, volume = 80) }
            confirmVerified(checkAvailable, speakText)
        }

    @Test
    fun `手動自己ベストラップ本文を再生する`() =
        runTest {
            val event = AceWindowsMyBestLapCurrentValue(83_456)
            coEvery { checkAvailable() } returns true
            coEvery { speakText(event.narratedText, volume = 80) } just Runs
            assertEquals(event.narratedText, speaker.readoutText(event))
            speaker(event, 80)
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText(event.narratedText, volume = 80) }
            confirmVerified(checkAvailable, speakText)
        }

    @Test
    fun `手動燃料現在値の本文と解決済み文言を再生する`() =
        runTest {
            val event = AceWindowsRemainingFuelCurrentValue(42)
            coEvery { checkAvailable() } returns true
            coEvery { speakText(event.narratedText, volume = 80) } just Runs

            assertEquals(event.narratedText, speaker.readoutText(event))
            speaker(event, 80)

            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText(event.narratedText, volume = 80) }
            confirmVerified(checkAvailable, speakText)
        }

    @Test
    fun `手動イベントの本文と解決済み文言を再生する`() =
        runTest {
            val event = AceWindowsManualReadoutText(AceWindowsReadoutItemKey.RemainingFuel.Root, "読み上げる項目がありません")
            coEvery { checkAvailable() } returns true
            coEvery { speakText(event.text, volume = 80) } just Runs
            coEvery { speakText("解決済み", volume = 80) } just Runs

            assertEquals(event.text, speaker.readoutText(event))
            speaker(event, 80)
            val resolved = event.withResolvedText("解決済み")
            assertEquals("解決済み", speaker.readoutText(resolved))
            speaker(resolved, 80)
            assertNull(speaker.readoutText(event.withResolvedText(" ")))
            speaker(event.withResolvedText(" "), 80)

            coVerify(exactly = 3) { checkAvailable() }
            coVerify(exactly = 1) { speakText(event.text, volume = 80) }
            coVerify(exactly = 1) { speakText("解決済み", volume = 80) }
            confirmVerified(checkAvailable, speakText)
        }

    @Test
    fun `保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsCheckeredFlag()))
            speaker(AceWindowsCheckeredFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf(text)
                assertNull(speaker.readoutText(AceWindowsCheckeredFlag()))
                speaker(AceWindowsCheckeredFlag(), 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `TTS利用不可なら読み上げない`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(AceWindowsCheckeredFlag()))
            speaker(AceWindowsCheckeredFlag(), 100)
            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `対象外イベントは設定を参照しない`() =
        runTest {
            val event = ForeignReadoutTextEvent()
            assertFalse(isAceWindowsCustomSpeakEvent(event))
            assertNull(speaker.readoutText(event))
            speaker(event, 100)
            verify(exactly = 0) { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `他シミュレーターの解決済み本文も読み上げない`() =
        runTest {
            val text = "他シミュレーターの本文"
            val event = ForeignReadoutTextEvent(resolvedText = text)
            assertFalse(isAceWindowsCustomSpeakEvent(event))
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            verify(exactly = 0) { observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(text, volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `全ACEイベントは自由文言対象になる`() {
        listOf(
            AceWindowsRemainingFuelLapsWarning(3),
            AceWindowsRemainingFuelLapsCurrentValue(3),
            AceWindowsRemainingFuelWarning(20),
            AceWindowsWhiteFlag(),
            AceWindowsGreenFlag(),
            AceWindowsRedFlag(),
            AceWindowsBlueFlag(),
            AceWindowsYellowFlag(),
            AceWindowsBlackFlag(),
            AceWindowsBlackWhiteFlag(),
            AceWindowsCheckeredFlag(),
            AceWindowsOrangeCircleFlag(),
            AceWindowsRedYellowStripesFlag(),
            AceWindowsTyreOverheat(110),
            AceWindowsVehicleApproach(),
            AceWindowsMyBestLap(83_456),
        ).forEach { assertTrue(isAceWindowsCustomSpeakEvent(it)) }
    }

    @Test
    fun `Whiteは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsWhiteFlag()))
            speaker(AceWindowsWhiteFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.WHITE) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `Greenは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsGreenFlag()))
            speaker(AceWindowsGreenFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.GREEN) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `Redは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsRedFlag()))
            speaker(AceWindowsRedFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.RED) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `Blueは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsBlueFlag()))
            speaker(AceWindowsBlueFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.BLUE) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `Yellowは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsYellowFlag()))
            speaker(AceWindowsYellowFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.YELLOW) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `Blackは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsBlackFlag()))
            speaker(AceWindowsBlackFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.BLACK) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `BlackWhiteは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsBlackWhiteFlag()))
            speaker(AceWindowsBlackWhiteFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `OrangeCircleは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsOrangeCircleFlag()))
            speaker(AceWindowsOrangeCircleFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `RedYellowStripesは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeFlagText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(AceWindowsRedYellowStripesFlag()))
            speaker(AceWindowsRedYellowStripesFlag(), 42)

            verify(exactly = 2) { observeFlagText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeFlagText, checkAvailable, speakText)
        }

    @Test
    fun `車両接近は保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeVehicleApproach() } returns flowOf("周囲に注意")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("周囲に注意", volume = 42) } just Runs

            assertEquals("周囲に注意", speaker.readoutText(AceWindowsVehicleApproach()))
            speaker(AceWindowsVehicleApproach(), 42)

            verify(exactly = 2) { observeVehicleApproach() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("周囲に注意", volume = 42) }
            confirmVerified(observeVehicleApproach, checkAvailable, speakText)
        }

    @Test
    fun `タイヤ過熱は保存文言の温度を整形して読み上げる`() =
        runTest {
            val event = AceWindowsTyreOverheat(111)
            every { observeTyreOverheat() } returns flowOf("過熱 {celsius}度、{celsius}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("過熱 111度、111", volume = 42) } just Runs
            assertEquals("過熱 111度、111", speaker.readoutText(event))
            speaker(event, 42)
            assertEquals(true, isAceWindowsCustomSpeakEvent(event))
            verify(exactly = 2) { observeTyreOverheat() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("過熱 111度、111", volume = 42) }
            confirmVerified(observeTyreOverheat, checkAvailable, speakText)
        }

    @Test
    fun `タイヤ過熱の解決済み本文は観測文言より優先し発話時にTTS利用可否を再確認しない`() =
        runTest {
            val event = AceWindowsTyreOverheat(111, "判定時の本文")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 42) } just Runs
            assertEquals("判定時の本文", speaker.readoutText(event))
            coEvery { checkAvailable() } returns false
            speaker(event, 42)
            verify(exactly = 0) { observeTyreOverheat() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 1) { speakText("判定時の本文", volume = 42) }
            confirmVerified(observeTyreOverheat, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量は保存文言の残量を整形して読み上げる`() =
        runTest {
            val event = AceWindowsRemainingFuelWarning(20)
            every { observeRemainingFuel() } returns flowOf("残り{percent}%、{percent}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("残り20%、20", volume = 42) } just Runs
            assertEquals("残り20%、20", speaker.readoutText(event))
            speaker(event, 42)
            assertEquals(true, isAceWindowsCustomSpeakEvent(event))
            verify(exactly = 2) { observeRemainingFuel() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("残り20%、20", volume = 42) }
            confirmVerified(observeRemainingFuel, checkAvailable, speakText)
        }

    @Test
    fun `燃料残り周回数は保存文言の残量を整形して読み上げる`() =
        runTest {
            val event = AceWindowsRemainingFuelLapsWarning(20)
            every { observeRemainingFuelLaps() } returns flowOf("残り{laps}周、{laps}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("残り20周、20", volume = 42) } just Runs
            assertEquals("残り20周、20", speaker.readoutText(event))
            speaker(event, 42)
            assertEquals(true, isAceWindowsCustomSpeakEvent(event))
            verify(exactly = 2) { observeRemainingFuelLaps() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("残り20周、20", volume = 42) }
            confirmVerified(observeRemainingFuelLaps, checkAvailable, speakText)
        }

    @Test
    fun `自己ベストラップは保存文言のラップタイムを整形して読み上げる`() =
        runTest {
            val event = AceWindowsMyBestLap(83_456)
            every { observeMyBestLap() } returns flowOf("更新 {laptime}、{laptime}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("更新 1分23秒456、1分23秒456", volume = 42) } just Runs
            assertEquals("更新 1分23秒456、1分23秒456", speaker.readoutText(event))
            speaker(event, 42)
            assertEquals(true, isAceWindowsCustomSpeakEvent(event))
            verify(exactly = 2) { observeMyBestLap() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("更新 1分23秒456、1分23秒456", volume = 42) }
            confirmVerified(observeMyBestLap, checkAvailable, speakText)
        }

    @Test
    fun `0周以下は燃料なし用文言をそのまま読み上げる`() =
        runTest {
            every { observeEmptyFuelLaps() } returns flowOf("燃料なし {laps}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("燃料なし {laps}", volume = 42) } just Runs
            listOf(0, -1).forEach { laps ->
                val event = AceWindowsRemainingFuelLapsWarning(laps)
                assertEquals("燃料なし {laps}", speaker.readoutText(event))
                speaker(event, 42)
            }
            verify(exactly = 4) { observeEmptyFuelLaps() }
            verify(exactly = 0) { observeRemainingFuelLaps() }
            coVerify(exactly = 4) { checkAvailable() }
            coVerify(exactly = 2) { speakText("燃料なし {laps}", volume = 42) }
            confirmVerified(observeEmptyFuelLaps, observeRemainingFuelLaps, checkAvailable, speakText)
        }

    @Test
    fun `解決済みフラッグと車両接近は保存文言とTTS可否を再取得せず読み上げる`() =
        runTest {
            val events =
                listOf(
                    AceWindowsCheckeredFlag("判定時の本文"),
                    AceWindowsWhiteFlag("判定時の本文"),
                    AceWindowsGreenFlag("判定時の本文"),
                    AceWindowsRedFlag("判定時の本文"),
                    AceWindowsBlueFlag("判定時の本文"),
                    AceWindowsYellowFlag("判定時の本文"),
                    AceWindowsBlackFlag("判定時の本文"),
                    AceWindowsBlackWhiteFlag("判定時の本文"),
                    AceWindowsOrangeCircleFlag("判定時の本文"),
                    AceWindowsRedYellowStripesFlag("判定時の本文"),
                    AceWindowsVehicleApproach("判定時の本文"),
                )
            coEvery { speakText("判定時の本文", volume = 42) } just Runs

            coEvery { checkAvailable() } returns true
            events.forEach { event ->
                assertEquals("判定時の本文", speaker.readoutText(event))
            }
            coEvery { checkAvailable() } returns false
            events.forEach { event -> speaker(event, 42) }

            coVerify(exactly = 11) { speakText("判定時の本文", volume = 42) }
            coVerify(exactly = 11) { checkAvailable() }
            verify(exactly = 0) {
                observeFlagText(AceWindowsFlagReadoutTextKey.CHECKERED)
                observeFlagText(AceWindowsFlagReadoutTextKey.WHITE)
                observeFlagText(AceWindowsFlagReadoutTextKey.GREEN)
                observeFlagText(AceWindowsFlagReadoutTextKey.RED)
                observeFlagText(AceWindowsFlagReadoutTextKey.BLUE)
                observeFlagText(AceWindowsFlagReadoutTextKey.YELLOW)
                observeFlagText(AceWindowsFlagReadoutTextKey.BLACK)
                observeFlagText(AceWindowsFlagReadoutTextKey.BLACK_WHITE)
                observeFlagText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE)
                observeFlagText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES)
                observeVehicleApproach()
            }
            confirmVerified(
                speakText,
                checkAvailable,
                observeFlagText,
                observeVehicleApproach,
            )
        }

    @Test
    fun `デバッグ要求の本文はテンプレート置換せずTTSで再生する`() =
        runTest {
            val key = ReadoutItemKey.topLevelEntriesOf(Simulator.AceWindows).first()
            val event = DebugReadoutText(key, "入力 {laps}")
            assertTrue(isAceWindowsCustomSpeakEvent(event))
            coEvery { checkAvailable() } returns true
            coEvery { speakText(event.resolvedText, volume = 42) } just Runs

            assertEquals(event.resolvedText, speaker.readoutText(event))
            speaker(event, 42)
            assertNull(speaker.readoutText(event.copy(resolvedText = " ")))
            speaker(event.copy(resolvedText = " "), 42)
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 1) { speakText(event.resolvedText, volume = 42) }
            confirmVerified(checkAvailable, speakText)
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
