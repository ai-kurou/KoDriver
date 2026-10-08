package kurou.kodriver.feature.gt7ps5narrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("TooManyFunctions")
class Gt7Ps5ReadoutTextSpeakerTest {
    private val observeText: ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase = mockk()
    private val observeEmptyText: ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase = mockk()
    private val observeFuelText: ObserveGt7Ps5RemainingFuelReadoutTextUseCase = mockk()
    private val observeTyreText: ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val observeMyBestLapText: ObserveGt7Ps5MyBestLapReadoutTextUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        Gt7Ps5ReadoutTextSpeaker(
            observeText,
            observeEmptyText,
            observeFuelText,
            observeMyBestLapText,
            observeTyreText,
            checkAvailable,
            speakText,
        )

    @Test
    fun `設定とTTS利用可否の取得が中断しても全種類の文言を解決して再生する`() =
        runTest {
            every { observeText() } returns
                flow {
                    delay(1)
                    emit("あと{laps}周")
                }
            every { observeEmptyText() } returns
                flow {
                    delay(1)
                    emit("燃料なし")
                }
            every { observeFuelText() } returns
                flow {
                    delay(1)
                    emit("残り{percent}%")
                }
            every { observeMyBestLapText() } returns
                flow {
                    delay(1)
                    emit("更新{laptime}")
                }
            every { observeTyreText() } returns
                flow {
                    delay(1)
                    emit("温度{celsius}度")
                }
            coEvery { checkAvailable() } coAnswers {
                delay(1)
                true
            }
            val events =
                listOf(
                    SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(3) to "あと3周",
                    SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0) to "燃料なし",
                    SpeechEvent.Gt7Ps5RemainingFuelWarning(30) to "残り30%",
                    SpeechEvent.Gt7Ps5MyBestLap(83_005) to "更新1分23秒005",
                    SpeechEvent.Gt7Ps5TyreOverheat(120) to "温度120度",
                )
            events.forEach { (_, text) ->
                coEvery { speakText(text, volume = 80) } coAnswers { delay(1) }
            }

            events.forEach { (event, text) ->
                assertEquals(text, speaker.readoutText(event))
                speaker(event, 80)
            }

            verify(exactly = 2) { observeText() }
            verify(exactly = 2) { observeEmptyText() }
            verify(exactly = 2) { observeFuelText() }
            verify(exactly = 2) { observeMyBestLapText() }
            verify(exactly = 2) { observeTyreText() }
            coVerify(exactly = 10) { checkAvailable() }
            events.forEach { (_, text) ->
                coVerify(exactly = 1) { speakText(text, volume = 80) }
            }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeMyBestLapText,
                observeTyreText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `通常文言の周回数を置換して音量付きで読み上げる`() =
        runTest {
            every { observeText() } returns flowOf("残り{laps}周・{laps}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("残り3周・3", volume = 42) } just Runs

            assertEquals("残り3周・3", speaker.readoutText(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(3)))
            speaker(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(3), 42)

            verify(exactly = 2) { observeText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("残り3周・3", volume = 42) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `0周以下は燃料なし文言をそのまま返す`() =
        runTest {
            every { observeEmptyText() } returns flowOf("燃料なし{laps}")
            coEvery { checkAvailable() } returns true

            listOf(0, -1, Int.MIN_VALUE).forEach { laps ->
                assertEquals("燃料なし{laps}", speaker.readoutText(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(laps)))
            }

            verify(exactly = 3) { observeEmptyText() }
            coVerify(exactly = 3) { checkAvailable() }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `解決済み文言を優先し設定を読み直さない`() =
        runTest {
            coEvery { speakText("確定した文言{laps}", volume = 80) } just Runs

            listOf(3, 0).forEach { laps ->
                speaker(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(laps, "確定した文言{laps}"), 80)
            }

            verify(exactly = 0) { observeText() }
            verify(exactly = 0) { observeEmptyText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 2) { speakText("確定した文言{laps}", volume = 80) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `空白文言は利用可否を確認せず読み上げない`() =
        runTest {
            every { observeText() } returns flowOf(" \t\n ")
            every { observeEmptyText() } returns flowOf("")
            val events =
                listOf(
                    SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(1),
                    SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(0),
                    SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(1, ""),
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
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `TTS利用不可なら判定時に解決済み文言もスキップする`() =
        runTest {
            coEvery { checkAvailable() } returns false
            val event = SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(3, "あと3周")

            assertNull(speaker.readoutText(event))

            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 0) { speakText("あと3周", volume = 100) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `対象外イベントでは設定とTTSを呼ばない`() =
        runTest {
            val events =
                listOf(
                    SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L),
                    SpeechEvent.AceWindowsRemainingFuelLapsWarning(3),
                    SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = 83_456L, resolvedText = "解決済み文言"),
                    SpeechEvent.AceWindowsRemainingFuelLapsWarning(3, resolvedText = "解決済み文言"),
                )
            events.forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }

            verify(exactly = 0) { observeText() }
            verify(exactly = 0) { observeEmptyText() }
            coVerify(exactly = 0) { checkAvailable() }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
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
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `燃料残量も解決済み文言を優先し設定を読み直さない`() =
        runTest {
            coEvery { speakText("確定した文言{percent}", volume = 80) } just Runs

            speaker(SpeechEvent.Gt7Ps5RemainingFuelWarning(30, "確定した文言{percent}"), 80)

            verify(exactly = 0) { observeFuelText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 1) { speakText("確定した文言{percent}", volume = 80) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
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
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `燃料残量はTTS利用不可なら判定時に保存文言も解決済み文言もスキップする`() =
        runTest {
            every { observeFuelText() } returns flowOf("残り{percent}%")
            coEvery { checkAvailable() } returns false
            listOf(
                SpeechEvent.Gt7Ps5RemainingFuelWarning(30),
                SpeechEvent.Gt7Ps5RemainingFuelWarning(30, "残り30%"),
            ).forEach { event ->
                assertNull(speaker.readoutText(event))
            }

            verify(exactly = 1) { observeFuelText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("残り30%", volume = 100) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `タイヤ過熱の保存文言を整数で置換して読み上げる`() =
        runTest {
            every { observeTyreText() } returns flowOf("温度{celsius}度・{celsius}{wheel}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("温度30度・30{wheel}", volume = 42) } just Runs

            assertEquals("温度30度・30{wheel}", speaker.readoutText(SpeechEvent.Gt7Ps5TyreOverheat(30)))
            speaker(SpeechEvent.Gt7Ps5TyreOverheat(30), 42)

            verify(exactly = 2) { observeTyreText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("温度30度・30{wheel}", volume = 42) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `タイヤ過熱も解決済み文言を優先し設定を読み直さない`() =
        runTest {
            coEvery { speakText("確定した文言{celsius}", volume = 80) } just Runs

            speaker(SpeechEvent.Gt7Ps5TyreOverheat(30, "確定した文言{celsius}"), 80)

            verify(exactly = 0) { observeTyreText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 1) { speakText("確定した文言{celsius}", volume = 80) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `タイヤ過熱の空白文言は利用可否を確認せず読み上げない`() =
        runTest {
            every { observeTyreText() } returns flowOf(" ")
            listOf(
                SpeechEvent.Gt7Ps5TyreOverheat(30),
                SpeechEvent.Gt7Ps5TyreOverheat(30, ""),
            ).forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }

            verify(exactly = 2) { observeTyreText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 100) }
            coVerify(exactly = 0) { speakText("", volume = 100) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `タイヤ過熱はTTS利用不可なら判定時に保存文言も解決済み文言もスキップする`() =
        runTest {
            every { observeTyreText() } returns flowOf("温度{celsius}度")
            coEvery { checkAvailable() } returns false
            listOf(
                SpeechEvent.Gt7Ps5TyreOverheat(30),
                SpeechEvent.Gt7Ps5TyreOverheat(30, "温度30度"),
            ).forEach { event ->
                assertNull(speaker.readoutText(event))
            }

            verify(exactly = 1) { observeTyreText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("温度30度", volume = 100) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `自己ベストラップ更新の保存文言を日本語のタイムに置換して読み上げる`() =
        runTest {
            every { observeMyBestLapText() } returns flowOf("更新{laptime}・{laptime}{wheel}")
            coEvery { checkAvailable() } returns true
            coEvery { speakText("更新1分23秒005・1分23秒005{wheel}", volume = 42) } just Runs

            assertEquals("更新1分23秒005・1分23秒005{wheel}", speaker.readoutText(SpeechEvent.Gt7Ps5MyBestLap(83_005)))
            speaker(SpeechEvent.Gt7Ps5MyBestLap(83_005), 42)

            verify(exactly = 2) { observeMyBestLapText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("更新1分23秒005・1分23秒005{wheel}", volume = 42) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `自己ベストラップ更新も解決済み文言を優先し設定を読み直さない`() =
        runTest {
            coEvery { speakText("確定した文言{laptime}", volume = 80) } just Runs

            speaker(SpeechEvent.Gt7Ps5MyBestLap(83_005, "確定した文言{laptime}"), 80)

            verify(exactly = 0) { observeMyBestLapText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 1) { speakText("確定した文言{laptime}", volume = 80) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `自己ベストラップ更新の空白文言は利用可否を確認せず読み上げない`() =
        runTest {
            every { observeMyBestLapText() } returns flowOf(" ")
            listOf(
                SpeechEvent.Gt7Ps5MyBestLap(83_005),
                SpeechEvent.Gt7Ps5MyBestLap(83_005, ""),
            ).forEach { event ->
                assertNull(speaker.readoutText(event))
                speaker(event, 100)
            }

            verify(exactly = 2) { observeMyBestLapText() }
            coVerify(exactly = 0) { checkAvailable() }
            coVerify(exactly = 0) { speakText(" ", volume = 100) }
            coVerify(exactly = 0) { speakText("", volume = 100) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `自己ベストラップ更新はTTS利用不可なら判定時に保存文言も解決済み文言もスキップする`() =
        runTest {
            every { observeMyBestLapText() } returns flowOf("更新{laptime}")
            coEvery { checkAvailable() } returns false
            listOf(
                SpeechEvent.Gt7Ps5MyBestLap(83_005),
                SpeechEvent.Gt7Ps5MyBestLap(83_005, "更新1分23秒005"),
            ).forEach { event ->
                assertNull(speaker.readoutText(event))
            }

            verify(exactly = 1) { observeMyBestLapText() }
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 0) { speakText("更新1分23秒005", volume = 100) }
            confirmVerified(
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
                checkAvailable,
                speakText,
            )
        }

    @Test
    fun `判定時に利用可否を一度確認し解決済み本文の再生では確認し直さない`() =
        runTest {
            every { observeText() } returns flowOf("あと{laps}周")
            coEvery { checkAvailable() } returnsMany listOf(true, false)
            coEvery { speakText("あと3周", volume = 80) } just Runs
            val event = SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(3)

            val text = speaker.readoutText(event)
            assertEquals("あと3周", text)
            speaker(event.copy(resolvedText = text), 80)

            verify(exactly = 1) { observeText() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 1) { speakText("あと3周", volume = 80) }
            confirmVerified(
                checkAvailable,
                speakText,
                observeText,
                observeEmptyText,
                observeFuelText,
                observeTyreText,
                observeMyBestLapText,
            )
        }

    @Test
    fun `未解決イベントの直接再生でもTTS利用不可なら本文を要求しない`() =
        runTest {
            every { observeText() } returns flowOf("あと{laps}周")
            coEvery { checkAvailable() } returns false

            speaker(SpeechEvent.Gt7Ps5RemainingFuelLapsWarning(3), 80)

            verify(exactly = 1) { observeText() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 0) { speakText("あと3周", volume = 80) }
            confirmVerified(observeText, checkAvailable, speakText)
        }
}
