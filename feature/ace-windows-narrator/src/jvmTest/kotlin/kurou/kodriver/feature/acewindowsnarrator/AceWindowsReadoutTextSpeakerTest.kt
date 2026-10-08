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
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AceWindowsReadoutTextSpeakerTest {
    private val observeText: ObserveAceWindowsCheckeredFlagReadoutTextUseCase = mockk()
    private val observeWhite: ObserveAceWindowsWhiteFlagReadoutTextUseCase = mockk()
    private val observeGreen: ObserveAceWindowsGreenFlagReadoutTextUseCase = mockk()
    private val observeRed: ObserveAceWindowsRedFlagReadoutTextUseCase = mockk()
    private val observeBlue: ObserveAceWindowsBlueFlagReadoutTextUseCase = mockk()
    private val observeYellow: ObserveAceWindowsYellowFlagReadoutTextUseCase = mockk()
    private val observeBlack: ObserveAceWindowsBlackFlagReadoutTextUseCase = mockk()
    private val observeBlackWhite: ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase = mockk()
    private val observeOrangeCircle: ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase = mockk()
    private val observeRedYellowStripes: ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase = mockk()
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
            observeText,
            observeWhite,
            observeGreen,
            observeRed,
            observeBlue,
            observeYellow,
            observeBlack,
            observeBlackWhite,
            observeOrangeCircle,
            observeRedYellowStripes,
            observeVehicleApproach,
            observeTyreOverheat,
            observeRemainingFuel,
            observeRemainingFuelLaps,
            observeEmptyFuelLaps,
            observeMyBestLap,
            checkAvailable,
            speakText,
        )

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
    fun `対象外イベントは設定を参照しない`() =
        runTest {
            val events =
                listOf(
                    SpeechEvent.AceWindowsMyBestLapFormal,
                )
            events.forEach { event ->
                assertFalse(isAceWindowsCustomSpeakEvent(event))
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }
            listOf(
                SpeechEvent.AceWindowsCheckeredFlag,
                SpeechEvent.AceWindowsOrangeCircleFlag,
                SpeechEvent.AceWindowsRedYellowStripesFlag,
            ).forEach { assertTrue(isAceWindowsCustomSpeakEvent(it)) }
            verify(exactly = 0) { observeText() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeText, checkAvailable, speakText)
        }

    @Test
    fun `Whiteは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeWhite() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsWhiteFlag))
            speaker(SpeechEvent.AceWindowsWhiteFlag, 42)

            verify(exactly = 2) { observeWhite() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeWhite, checkAvailable, speakText)
        }

    @Test
    fun `Whiteは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeWhite() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsWhiteFlag))
                speaker(SpeechEvent.AceWindowsWhiteFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeWhite() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeWhite, checkAvailable, speakText)
        }

    @Test
    fun `WhiteはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeWhite() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsWhiteFlag))
            speaker(SpeechEvent.AceWindowsWhiteFlag, 100)
            verify(exactly = 2) { observeWhite() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeWhite, checkAvailable, speakText)
        }

    @Test
    fun `Greenは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeGreen() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsGreenFlag))
            speaker(SpeechEvent.AceWindowsGreenFlag, 42)

            verify(exactly = 2) { observeGreen() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeGreen, checkAvailable, speakText)
        }

    @Test
    fun `Greenは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeGreen() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsGreenFlag))
                speaker(SpeechEvent.AceWindowsGreenFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeGreen() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeGreen, checkAvailable, speakText)
        }

    @Test
    fun `GreenはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeGreen() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsGreenFlag))
            speaker(SpeechEvent.AceWindowsGreenFlag, 100)
            verify(exactly = 2) { observeGreen() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeGreen, checkAvailable, speakText)
        }

    @Test
    fun `Redは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeRed() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsRedFlag))
            speaker(SpeechEvent.AceWindowsRedFlag, 42)

            verify(exactly = 2) { observeRed() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeRed, checkAvailable, speakText)
        }

    @Test
    fun `Redは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeRed() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsRedFlag))
                speaker(SpeechEvent.AceWindowsRedFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeRed() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeRed, checkAvailable, speakText)
        }

    @Test
    fun `RedはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeRed() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsRedFlag))
            speaker(SpeechEvent.AceWindowsRedFlag, 100)
            verify(exactly = 2) { observeRed() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeRed, checkAvailable, speakText)
        }

    @Test
    fun `Blueは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeBlue() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsBlueFlag))
            speaker(SpeechEvent.AceWindowsBlueFlag, 42)

            verify(exactly = 2) { observeBlue() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeBlue, checkAvailable, speakText)
        }

    @Test
    fun `Blueは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeBlue() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsBlueFlag))
                speaker(SpeechEvent.AceWindowsBlueFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeBlue() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeBlue, checkAvailable, speakText)
        }

    @Test
    fun `BlueはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeBlue() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsBlueFlag))
            speaker(SpeechEvent.AceWindowsBlueFlag, 100)
            verify(exactly = 2) { observeBlue() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeBlue, checkAvailable, speakText)
        }

    @Test
    fun `Yellowは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeYellow() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsYellowFlag))
            speaker(SpeechEvent.AceWindowsYellowFlag, 42)

            verify(exactly = 2) { observeYellow() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeYellow, checkAvailable, speakText)
        }

    @Test
    fun `Yellowは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeYellow() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsYellowFlag))
                speaker(SpeechEvent.AceWindowsYellowFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeYellow() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeYellow, checkAvailable, speakText)
        }

    @Test
    fun `YellowはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeYellow() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsYellowFlag))
            speaker(SpeechEvent.AceWindowsYellowFlag, 100)
            verify(exactly = 2) { observeYellow() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeYellow, checkAvailable, speakText)
        }

    @Test
    fun `Blackは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeBlack() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsBlackFlag))
            speaker(SpeechEvent.AceWindowsBlackFlag, 42)

            verify(exactly = 2) { observeBlack() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeBlack, checkAvailable, speakText)
        }

    @Test
    fun `Blackは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeBlack() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsBlackFlag))
                speaker(SpeechEvent.AceWindowsBlackFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeBlack() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeBlack, checkAvailable, speakText)
        }

    @Test
    fun `BlackはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeBlack() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsBlackFlag))
            speaker(SpeechEvent.AceWindowsBlackFlag, 100)
            verify(exactly = 2) { observeBlack() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeBlack, checkAvailable, speakText)
        }

    @Test
    fun `BlackWhiteは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeBlackWhite() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsBlackWhiteFlag))
            speaker(SpeechEvent.AceWindowsBlackWhiteFlag, 42)

            verify(exactly = 2) { observeBlackWhite() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeBlackWhite, checkAvailable, speakText)
        }

    @Test
    fun `BlackWhiteは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeBlackWhite() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsBlackWhiteFlag))
                speaker(SpeechEvent.AceWindowsBlackWhiteFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeBlackWhite() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeBlackWhite, checkAvailable, speakText)
        }

    @Test
    fun `BlackWhiteはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeBlackWhite() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsBlackWhiteFlag))
            speaker(SpeechEvent.AceWindowsBlackWhiteFlag, 100)
            verify(exactly = 2) { observeBlackWhite() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeBlackWhite, checkAvailable, speakText)
        }

    @Test
    fun `OrangeCircleは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeOrangeCircle() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsOrangeCircleFlag))
            speaker(SpeechEvent.AceWindowsOrangeCircleFlag, 42)

            verify(exactly = 2) { observeOrangeCircle() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeOrangeCircle, checkAvailable, speakText)
        }

    @Test
    fun `OrangeCircleは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeOrangeCircle() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsOrangeCircleFlag))
                speaker(SpeechEvent.AceWindowsOrangeCircleFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeOrangeCircle() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeOrangeCircle, checkAvailable, speakText)
        }

    @Test
    fun `OrangeCircleはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeOrangeCircle() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsOrangeCircleFlag))
            speaker(SpeechEvent.AceWindowsOrangeCircleFlag, 100)
            verify(exactly = 2) { observeOrangeCircle() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeOrangeCircle, checkAvailable, speakText)
        }

    @Test
    fun `RedYellowStripesは保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeRedYellowStripes() } returns flowOf("チェッカー、完走")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("チェッカー、完走", volume = 42) } just Runs

            assertEquals("チェッカー、完走", speaker.readoutText(SpeechEvent.AceWindowsRedYellowStripesFlag))
            speaker(SpeechEvent.AceWindowsRedYellowStripesFlag, 42)

            verify(exactly = 2) { observeRedYellowStripes() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("チェッカー、完走", volume = 42) }
            confirmVerified(observeRedYellowStripes, checkAvailable, speakText)
        }

    @Test
    fun `RedYellowStripesは空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeRedYellowStripes() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsRedYellowStripesFlag))
                speaker(SpeechEvent.AceWindowsRedYellowStripesFlag, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeRedYellowStripes() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeRedYellowStripes, checkAvailable, speakText)
        }

    @Test
    fun `RedYellowStripesはTTS利用不可なら読み上げない`() =
        runTest {
            every { observeRedYellowStripes() } returns flowOf("完走")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsRedYellowStripesFlag))
            speaker(SpeechEvent.AceWindowsRedYellowStripesFlag, 100)
            verify(exactly = 2) { observeRedYellowStripes() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("完走", volume = 100) }
            confirmVerified(observeRedYellowStripes, checkAvailable, speakText)
        }

    @Test
    fun `車両接近は保存文言をそのまま指定音量で読み上げる`() =
        runTest {
            every { observeVehicleApproach() } returns flowOf("周囲に注意")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("周囲に注意", volume = 42) } just Runs

            assertEquals("周囲に注意", speaker.readoutText(SpeechEvent.AceWindowsVehicleApproach))
            speaker(SpeechEvent.AceWindowsVehicleApproach, 42)

            verify(exactly = 2) { observeVehicleApproach() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("周囲に注意", volume = 42) }
            confirmVerified(observeVehicleApproach, checkAvailable, speakText)
        }

    @Test
    fun `車両接近は空文字と空白では利用可否を確認せず読み上げない`() =
        runTest {
            listOf("", " \t\n ").forEach { text ->
                every { observeVehicleApproach() } returns flowOf(text)
                assertNull(speaker.readoutText(SpeechEvent.AceWindowsVehicleApproach))
                speaker(SpeechEvent.AceWindowsVehicleApproach, 100)
                coVerify(exactly = 0) { speakText(text, volume = 100) }
            }
            verify(exactly = 4) { observeVehicleApproach() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeVehicleApproach, checkAvailable, speakText)
        }

    @Test
    fun `車両接近はTTS利用不可なら読み上げない`() =
        runTest {
            every { observeVehicleApproach() } returns flowOf("接近")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(SpeechEvent.AceWindowsVehicleApproach))
            speaker(SpeechEvent.AceWindowsVehicleApproach, 100)
            verify(exactly = 2) { observeVehicleApproach() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("接近", volume = 100) }
            confirmVerified(observeVehicleApproach, checkAvailable, speakText)
        }

    @Test
    fun `タイヤ過熱は保存文言の温度を整形して読み上げる`() =
        runTest {
            val event = SpeechEvent.AceWindowsTyreOverheat(111)
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
            val event = SpeechEvent.AceWindowsTyreOverheat(111, "判定時の本文")
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
    fun `タイヤ過熱は空白の保存文言や解決済み本文を読み上げない`() =
        runTest {
            every { observeTyreOverheat() } returns flowOf(" ")
            val event = SpeechEvent.AceWindowsTyreOverheat(111)
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            speaker(event.withResolvedText(" "), 42)
            verify(exactly = 2) { observeTyreOverheat() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 42) }
            confirmVerified(observeTyreOverheat, checkAvailable, speakText)
        }

    @Test
    fun `タイヤ過熱はTTS利用不可なら読み上げない`() =
        runTest {
            val event = SpeechEvent.AceWindowsTyreOverheat(111)
            every { observeTyreOverheat() } returns flowOf("過熱 {celsius}度")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            verify(exactly = 2) { observeTyreOverheat() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("過熱 111度", volume = 42) }
            confirmVerified(observeTyreOverheat, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量は保存文言の残量を整形して読み上げる`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelWarning(20)
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
    fun `燃料残量の解決済み本文は観測文言より優先し発話時にTTS利用可否を再確認しない`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelWarning(20, "判定時の本文")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 42) } just Runs
            assertEquals("判定時の本文", speaker.readoutText(event))
            coEvery { checkAvailable() } returns false
            speaker(event, 42)
            verify(exactly = 0) { observeRemainingFuel() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 1) { speakText("判定時の本文", volume = 42) }
            confirmVerified(observeRemainingFuel, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量は空白の保存文言や解決済み本文を読み上げない`() =
        runTest {
            every { observeRemainingFuel() } returns flowOf(" ")
            val event = SpeechEvent.AceWindowsRemainingFuelWarning(20)
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            speaker(event.withResolvedText(" "), 42)
            verify(exactly = 2) { observeRemainingFuel() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 42) }
            confirmVerified(observeRemainingFuel, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量はTTS利用不可なら読み上げない`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelWarning(20)
            every { observeRemainingFuel() } returns flowOf("残り{percent}%")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            verify(exactly = 2) { observeRemainingFuel() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("残り20%", volume = 42) }
            confirmVerified(observeRemainingFuel, checkAvailable, speakText)
        }

    @Test
    fun `燃料残り周回数は保存文言の残量を整形して読み上げる`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(20)
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
    fun `燃料残り周回数の解決済み本文は観測文言より優先し発話時にTTS利用可否を再確認しない`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(20, "判定時の本文")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 42) } just Runs
            assertEquals("判定時の本文", speaker.readoutText(event))
            coEvery { checkAvailable() } returns false
            speaker(event, 42)
            verify(exactly = 0) { observeRemainingFuelLaps() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 1) { speakText("判定時の本文", volume = 42) }
            confirmVerified(observeRemainingFuelLaps, checkAvailable, speakText)
        }

    @Test
    fun `燃料残り周回数は空白の保存文言や解決済み本文を読み上げない`() =
        runTest {
            every { observeRemainingFuelLaps() } returns flowOf(" ")
            val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(20)
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            speaker(event.withResolvedText(" "), 42)
            verify(exactly = 2) { observeRemainingFuelLaps() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 42) }
            confirmVerified(observeRemainingFuelLaps, checkAvailable, speakText)
        }

    @Test
    fun `燃料残り周回数はTTS利用不可なら読み上げない`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(20)
            every { observeRemainingFuelLaps() } returns flowOf("残り{laps}周")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            verify(exactly = 2) { observeRemainingFuelLaps() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("残り20周", volume = 42) }
            confirmVerified(observeRemainingFuelLaps, checkAvailable, speakText)
        }

    @Test
    fun `自己ベストラップは保存文言のラップタイムを整形して読み上げる`() =
        runTest {
            val event = SpeechEvent.AceWindowsMyBestLap(83_456)
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
    fun `自己ベストラップの解決済み本文は観測文言より優先し発話時にTTS利用可否を再確認しない`() =
        runTest {
            val event = SpeechEvent.AceWindowsMyBestLap(83_456, "判定時の本文")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 42) } just Runs
            assertEquals("判定時の本文", speaker.readoutText(event))
            coEvery { checkAvailable() } returns false
            speaker(event, 42)
            verify(exactly = 0) { observeMyBestLap() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 1) { speakText("判定時の本文", volume = 42) }
            confirmVerified(observeMyBestLap, checkAvailable, speakText)
        }

    @Test
    fun `自己ベストラップは空白の保存文言や解決済み本文を読み上げない`() =
        runTest {
            every { observeMyBestLap() } returns flowOf(" ")
            val event = SpeechEvent.AceWindowsMyBestLap(83_456)
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            speaker(event.withResolvedText(" "), 42)
            verify(exactly = 2) { observeMyBestLap() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 42) }
            confirmVerified(observeMyBestLap, checkAvailable, speakText)
        }

    @Test
    fun `自己ベストラップはTTS利用不可なら読み上げない`() =
        runTest {
            val event = SpeechEvent.AceWindowsMyBestLap(83_456)
            every { observeMyBestLap() } returns flowOf("更新 {laptime}")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            verify(exactly = 2) { observeMyBestLap() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("更新 1分23秒456", volume = 42) }
            confirmVerified(observeMyBestLap, checkAvailable, speakText)
        }

    @Test
    fun `0周以下は燃料なし用文言をそのまま読み上げる`() =
        runTest {
            every { observeEmptyFuelLaps() } returns flowOf("燃料なし {laps}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("燃料なし {laps}", volume = 42) } just Runs
            listOf(0, -1).forEach { laps ->
                val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(laps)
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
    fun `0周用の空白文言はTTSを確認せずスキップしTTS不可の文言も読み上げない`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(0)
            every { observeEmptyFuelLaps() } returns flowOf(" ")
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            every { observeEmptyFuelLaps() } returns flowOf("燃料なし")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(event))
            speaker(event, 42)
            verify(exactly = 4) { observeEmptyFuelLaps() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 42) }
            coVerify(exactly = 0) { speakText("燃料なし", volume = 42) }
            confirmVerified(observeEmptyFuelLaps, checkAvailable, speakText)
        }

    @Test
    fun `0周用も解決済み本文を優先する`() =
        runTest {
            val event = SpeechEvent.AceWindowsRemainingFuelLapsWarning(0, "確定した燃料なし")
            coEvery { speakText("確定した燃料なし", volume = 42) } just Runs
            speaker(event, 42)
            verify(exactly = 0) { observeEmptyFuelLaps() }
            verify(exactly = 0) { observeRemainingFuelLaps() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 1) { speakText("確定した燃料なし", volume = 42) }
            confirmVerified(observeEmptyFuelLaps, observeRemainingFuelLaps, checkAvailable, speakText)
        }
}
