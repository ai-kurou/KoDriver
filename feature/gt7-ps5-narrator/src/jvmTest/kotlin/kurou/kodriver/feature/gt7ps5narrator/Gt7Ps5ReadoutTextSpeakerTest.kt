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
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsWarning
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5ManualReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelCurrentValue
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsCurrentValue
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsWarning
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelWarning
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import kurou.kodriver.domain.engine.Gt7Ps5TyreTemperatureCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
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
            Gt7Ps5ReadoutTextUseCases(
                remainingFuelLaps = observeText,
                remainingFuelLapsEmpty = observeEmptyText,
                remainingFuel = observeFuelText,
                myBestLap = observeMyBestLapText,
                tyreOverheat = observeTyreText,
            ),
            checkAvailable,
            speakText,
        )

    @Test
    fun `手動タイヤ温度は自動読み上げ文言を参照せず既定文言と解決済み文言を読む`() =
        runTest {
            val event = Gt7Ps5TyreTemperatureCurrentValue(90)
            coEvery { checkAvailable() } returns true
            coEvery { speakText("タイヤ温度90℃", volume = 80) } just Runs
            coEvery { speakText("温度90℃", volume = 80) } just Runs
            assertEquals("タイヤ温度90℃", speaker.readoutText(event))
            speaker(event, 80)
            speaker(event.withResolvedText("温度90℃"), 80)
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("タイヤ温度90℃", volume = 80) }
            coVerify(exactly = 1) { speakText("温度90℃", volume = 80) }
            confirmVerified(checkAvailable, speakText, observeText, observeEmptyText)
        }

    @Test
    fun `手動燃料周回数は自動読み上げ文言を参照せず既定文言と解決済み文言を読む`() =
        runTest {
            val event = Gt7Ps5RemainingFuelLapsCurrentValue(3)
            coEvery { checkAvailable() } returns true
            coEvery { speakText("燃料は残り約3周", volume = 80) } just Runs
            coEvery { speakText("あと3周", volume = 80) } just Runs
            assertEquals("燃料は残り約3周", speaker.readoutText(event))
            speaker(event, 80)
            speaker(event.withResolvedText("あと3周"), 80)
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("燃料は残り約3周", volume = 80) }
            coVerify(exactly = 1) { speakText("あと3周", volume = 80) }
            confirmVerified(checkAvailable, speakText, observeText, observeEmptyText)
        }

    @Test
    fun `手動燃料残量は自動読み上げ文言を参照せず既定文言と解決済み文言を読む`() =
        runTest {
            val event = Gt7Ps5RemainingFuelCurrentValue(42)
            coEvery { checkAvailable() } returns true
            coEvery { speakText("燃料は残り42パーセント", volume = 80) } just Runs
            coEvery { speakText("残り42%", volume = 80) } just Runs
            assertEquals("燃料は残り42パーセント", speaker.readoutText(event))
            speaker(event, 80)
            speaker(event.withResolvedText("残り42%"), 80)
            coVerify(exactly = 2) { checkAvailable() }
            coVerify(exactly = 1) { speakText("燃料は残り42パーセント", volume = 80) }
            coVerify(exactly = 1) { speakText("残り42%", volume = 80) }
            confirmVerified(checkAvailable, speakText, observeFuelText)
        }

    @Test
    fun `手動イベントの本文と解決済み文言を再生する`() =
        runTest {
            val event = Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, "読み上げる項目がありません")
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
    fun `TTS利用不可なら手動イベントを読み上げない`() =
        runTest {
            val event = Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, "読み上げる項目がありません")
            coEvery { checkAvailable() } returns false
            assertNull(speaker.readoutText(event))
            speaker(event, 80)
            coVerify(exactly = 2) { checkAvailable() }
            confirmVerified(checkAvailable, speakText)
        }

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
                    Gt7Ps5RemainingFuelLapsWarning(3) to "あと3周",
                    Gt7Ps5RemainingFuelLapsWarning(0) to "燃料なし",
                    Gt7Ps5RemainingFuelWarning(30) to "残り30%",
                    Gt7Ps5MyBestLap(83_005) to "更新1分23秒005",
                    Gt7Ps5TyreOverheat(120) to "温度120度",
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

            assertEquals("残り3周・3", speaker.readoutText(Gt7Ps5RemainingFuelLapsWarning(3)))
            speaker(Gt7Ps5RemainingFuelLapsWarning(3), 42)

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
                assertEquals("燃料なし{laps}", speaker.readoutText(Gt7Ps5RemainingFuelLapsWarning(laps)))
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
                speaker(Gt7Ps5RemainingFuelLapsWarning(laps, "確定した文言{laps}"), 80)
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
                    Gt7Ps5RemainingFuelLapsWarning(1),
                    Gt7Ps5RemainingFuelLapsWarning(0),
                    Gt7Ps5RemainingFuelLapsWarning(1, ""),
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
            val event = Gt7Ps5RemainingFuelLapsWarning(3, "あと3周")

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
                    LmuWindowsMyBestLap(lapTimeMs = 83_456L),
                    AceWindowsRemainingFuelLapsWarning(3),
                    LmuWindowsMyBestLap(lapTimeMs = 83_456L, resolvedText = "解決済み文言"),
                    AceWindowsRemainingFuelLapsWarning(3, resolvedText = "解決済み文言"),
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

            assertEquals("残り30%・30", speaker.readoutText(Gt7Ps5RemainingFuelWarning(30)))
            speaker(Gt7Ps5RemainingFuelWarning(30), 42)

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

            speaker(Gt7Ps5RemainingFuelWarning(30, "確定した文言{percent}"), 80)

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
                Gt7Ps5RemainingFuelWarning(30),
                Gt7Ps5RemainingFuelWarning(30, ""),
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
                Gt7Ps5RemainingFuelWarning(30),
                Gt7Ps5RemainingFuelWarning(30, "残り30%"),
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

            assertEquals("温度30度・30{wheel}", speaker.readoutText(Gt7Ps5TyreOverheat(30)))
            speaker(Gt7Ps5TyreOverheat(30), 42)

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

            speaker(Gt7Ps5TyreOverheat(30, "確定した文言{celsius}"), 80)

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
                Gt7Ps5TyreOverheat(30),
                Gt7Ps5TyreOverheat(30, ""),
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
                Gt7Ps5TyreOverheat(30),
                Gt7Ps5TyreOverheat(30, "温度30度"),
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

            assertEquals("更新1分23秒005・1分23秒005{wheel}", speaker.readoutText(Gt7Ps5MyBestLap(83_005)))
            speaker(Gt7Ps5MyBestLap(83_005), 42)

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

            speaker(Gt7Ps5MyBestLap(83_005, "確定した文言{laptime}"), 80)

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
                Gt7Ps5MyBestLap(83_005),
                Gt7Ps5MyBestLap(83_005, ""),
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
                Gt7Ps5MyBestLap(83_005),
                Gt7Ps5MyBestLap(83_005, "更新1分23秒005"),
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
            val event = Gt7Ps5RemainingFuelLapsWarning(3)

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

            speaker(Gt7Ps5RemainingFuelLapsWarning(3), 80)

            verify(exactly = 1) { observeText() }
            coVerify(exactly = 1) { checkAvailable() }
            coVerify(exactly = 0) { speakText("あと3周", volume = 80) }
            confirmVerified(observeText, checkAvailable, speakText)
        }

    @Test
    fun `デバッグ要求の本文はテンプレート置換せずTTSで再生する`() =
        runTest {
            val key = ReadoutItemKey.topLevelEntriesOf(Simulator.Gt7Ps5).first()
            val event = DebugReadoutText(key, "入力 {laps}")
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
