package kurou.kodriver.feature.gt7ps5narrator

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
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Gt7Ps5ReadoutTextSpeakerTest {
    private val observeText: ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase = mockk()
    private val observeEmptyText: ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase = mockk()
    private val observeFuelText: ObserveGt7Ps5RemainingFuelReadoutTextUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        Gt7Ps5ReadoutTextSpeaker(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)

    @Test
    fun `通常文言の周回数を置換して音量付きで読み上げる`() =
        runTest {
            every { observeText() } returns flowOf("残り{laps}周・{laps}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("残り3周・3", volume = 42) } just Runs

            assertEquals("残り3周・3", speaker.readoutText(SpeechEvent.RemainingFuelLapsWarning(3)))
            speaker(SpeechEvent.RemainingFuelLapsWarning(3), 42)

            verify(exactly = 2) { observeText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("残り3周・3", volume = 42) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `0周以下は燃料なし文言をそのまま返す`() =
        runTest {
            every { observeEmptyText() } returns flowOf("燃料なし{laps}")
            coEvery { checkAvailable() } returns true

            listOf(0, -1, Int.MIN_VALUE).forEach { laps ->
                assertEquals("燃料なし{laps}", speaker.readoutText(SpeechEvent.RemainingFuelLapsWarning(laps)))
            }

            verify(exactly = 3) { observeEmptyText() }
            coVerify(exactly = 3) { checkAvailable() }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `解決済み文言を優先し設定を読み直さない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            coEvery { speakText("確定した文言{laps}", volume = 80) } just Runs

            listOf(3, 0).forEach { laps ->
                speaker(SpeechEvent.RemainingFuelLapsWarning(laps, "確定した文言{laps}"), 80)
            }

            verify(exactly = 0) { observeText() }
            verify(exactly = 0) { observeEmptyText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 2) { speakText("確定した文言{laps}", volume = 80) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `空白文言は利用可否を確認せず読み上げない`() =
        runTest {
            every { observeText() } returns flowOf(" \t\n ")
            every { observeEmptyText() } returns flowOf("")
            val events =
                listOf(
                    SpeechEvent.RemainingFuelLapsWarning(1),
                    SpeechEvent.RemainingFuelLapsWarning(0),
                    SpeechEvent.RemainingFuelLapsWarning(1, ""),
                )
            events.forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }

            verify(exactly = 2) { observeText() }
            verify(exactly = 2) { observeEmptyText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" \t\n ", volume = 100) }
            coVerify(exactly = 0) { speakText("", volume = 100) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `TTS利用不可なら解決済み文言も読み上げない`() =
        runTest {
            coEvery { checkAvailable() } returns false
            val event = SpeechEvent.RemainingFuelLapsWarning(3, "あと3周")

            assertNull(speaker.readoutText(event))
            speaker(event, 100)

            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("あと3周", volume = 100) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `対象外イベントでは設定とTTSを呼ばない`() =
        runTest {
            val events =
                listOf(
                    SpeechEvent.Gt7Ps5MyBestLapFormal,
                    SpeechEvent.Gt7Ps5TyreOverheat,
                    SpeechEvent.AceWindowsRemainingFuelLapsWarning(3),
                )
            events.forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }

            verify(exactly = 0) { observeText() }
            verify(exactly = 0) { observeEmptyText() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量の保存文言を整数で置換して読み上げる`() =
        runTest {
            every { observeFuelText() } returns flowOf("残り{percent}%・{percent}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("残り30%・30", volume = 42) } just Runs

            assertEquals("残り30%・30", speaker.readoutText(SpeechEvent.Gt7Ps5RemainingFuelWarning(30)))
            speaker(SpeechEvent.Gt7Ps5RemainingFuelWarning(30), 42)

            verify(exactly = 2) { observeFuelText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("残り30%・30", volume = 42) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量も解決済み文言を優先し設定を読み直さない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            coEvery { speakText("確定した文言{percent}", volume = 80) } just Runs

            speaker(SpeechEvent.Gt7Ps5RemainingFuelWarning(30, "確定した文言{percent}"), 80)

            verify(exactly = 0) { observeFuelText() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 1) { speakText("確定した文言{percent}", volume = 80) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量の空白文言は利用可否を確認せず読み上げない`() =
        runTest {
            every { observeFuelText() } returns flowOf(" ")
            listOf(
                SpeechEvent.Gt7Ps5RemainingFuelWarning(30),
                SpeechEvent.Gt7Ps5RemainingFuelWarning(30, ""),
            ).forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }

            verify(exactly = 2) { observeFuelText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 100) }
            coVerify(exactly = 0) { speakText("", volume = 100) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }

    @Test
    fun `燃料残量はTTS利用不可なら保存文言も解決済み文言も読み上げない`() =
        runTest {
            every { observeFuelText() } returns flowOf("残り{percent}%")
            coEvery { checkAvailable() } returns false
            listOf(
                SpeechEvent.Gt7Ps5RemainingFuelWarning(30),
                SpeechEvent.Gt7Ps5RemainingFuelWarning(30, "残り30%"),
            ).forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }

            verify(exactly = 2) { observeFuelText() }
            coVerify(exactly = 4) { checkAvailable() }
            coVerify(exactly = 0) { speakText("残り30%", volume = 100) }
            confirmVerified(observeText, observeEmptyText, observeFuelText, checkAvailable, speakText)
        }
}
