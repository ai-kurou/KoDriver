@file:Suppress("TooManyFunctions")

package kurou.kodriver.feature.acewindowsreadout.flagdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutFlagDetailCommonFlagViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AceWindowsFlagPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val texts: AceWindowsFlagReadoutTextPreferencesRepository = mockk()
    private val tts: TextToSpeechRepository = mockk()
    private val observeVoice: ObserveVoiceUseCase = mockk()
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase = mockk()
    private val observeVoicePitch: ObserveVoicePitchUseCase = mockk()
    private val volumes: SoundVolumePreferencesRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutFlagDetailViewModel(
            settingsUseCases =
                FlagSettingsUseCases(
                    ObserveAceWindowsFlagEnabledStatesUseCase(repository),
                    SaveAceWindowsFlagEnabledStateUseCase(repository),
                    FlagReadoutTextUseCases(
                        ObserveAceWindowsCheckeredFlagReadoutTextUseCase(texts),
                        SaveAceWindowsCheckeredFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsWhiteFlagReadoutTextUseCase(texts),
                        SaveAceWindowsWhiteFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsGreenFlagReadoutTextUseCase(texts),
                        SaveAceWindowsGreenFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsRedFlagReadoutTextUseCase(texts),
                        SaveAceWindowsRedFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsBlueFlagReadoutTextUseCase(texts),
                        SaveAceWindowsBlueFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsYellowFlagReadoutTextUseCase(texts),
                        SaveAceWindowsYellowFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsBlackFlagReadoutTextUseCase(texts),
                        SaveAceWindowsBlackFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase(texts),
                        SaveAceWindowsBlackWhiteFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase(texts),
                        SaveAceWindowsOrangeCircleFlagReadoutTextUseCase(texts),
                        ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase(texts),
                        SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(texts),
                    ),
                ),
            speakText = SpeakTextUseCase(tts, observeVoice, observeVoiceSpeed, observeVoicePitch),
            playStartSoundForKey = PlayStartSoundForKeyUseCase(ttsEngine),
            checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(tts),
            observeSoundVolume = ObserveSoundVolumeUseCase(volumes),
        )

    @Test
    fun `Whiteの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.WHITE, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.WHITE, "ホワイトフラッグ") } answers {
                textFlow.update { "ホワイトフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.WhiteFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.WhiteFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.WhiteFlag))
            vm.onFlagTextReset(FlagReadoutItem.WhiteFlag)
            assertEquals("ホワイトフラッグ", vm.uiState.first().flagText(FlagReadoutItem.WhiteFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.WHITE, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.WHITE, "ホワイトフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `Greenの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.GREEN, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.GREEN, "グリーンフラッグ") } answers {
                textFlow.update { "グリーンフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.GreenFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.GreenFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.GreenFlag))
            vm.onFlagTextReset(FlagReadoutItem.GreenFlag)
            assertEquals("グリーンフラッグ", vm.uiState.first().flagText(FlagReadoutItem.GreenFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.GREEN, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.GREEN, "グリーンフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `Redの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.RED, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.RED, "レッドフラッグ") } answers {
                textFlow.update { "レッドフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.RedFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.RedFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.RedFlag))
            vm.onFlagTextReset(FlagReadoutItem.RedFlag)
            assertEquals("レッドフラッグ", vm.uiState.first().flagText(FlagReadoutItem.RedFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.RED, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.RED, "レッドフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `Blueの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.BLUE, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.BLUE, "ブルーフラッグ") } answers {
                textFlow.update { "ブルーフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.BlueFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.BlueFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.BlueFlag))
            vm.onFlagTextReset(FlagReadoutItem.BlueFlag)
            assertEquals("ブルーフラッグ", vm.uiState.first().flagText(FlagReadoutItem.BlueFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.BLUE, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.BLUE, "ブルーフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `Yellowの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "イエローフラッグ") } answers {
                textFlow.update { "イエローフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.YellowFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.YellowFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.YellowFlag))
            vm.onFlagTextReset(FlagReadoutItem.YellowFlag)
            assertEquals("イエローフラッグ", vm.uiState.first().flagText(FlagReadoutItem.YellowFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "イエローフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `Blackの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK, "ブラックフラッグ") } answers {
                textFlow.update { "ブラックフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.BlackFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.BlackFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.BlackFlag))
            vm.onFlagTextReset(FlagReadoutItem.BlackFlag)
            assertEquals("ブラックフラッグ", vm.uiState.first().flagText(FlagReadoutItem.BlackFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK, "ブラックフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            confirmVerified(texts)
        }

    @Test
    fun `BlackWhiteの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "") } answers { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "ブラック・ホワイトフラッグ") } answers {
                textFlow.update { "ブラック・ホワイトフラッグ" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.BlackWhiteFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.BlackWhiteFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.BlackWhiteFlag))
            vm.onFlagTextReset(FlagReadoutItem.BlackWhiteFlag)
            assertEquals("ブラック・ホワイトフラッグ", vm.uiState.first().flagText(FlagReadoutItem.BlackWhiteFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "") }
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "ブラック・ホワイトフラッグ") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            confirmVerified(texts)
        }

    @Test
    fun `OrangeCircleの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns
                flowOf("レッド・イエローストライプフラッグ、路面が滑りやすいです")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "") } answers
                { textFlow.update { "" } }
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "オレンジボールフラッグ、車両に不具合があります") } answers {
                textFlow.update { "オレンジボールフラッグ、車両に不具合があります" }
            }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.OrangeCircleFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.OrangeCircleFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.OrangeCircleFlag))
            vm.onFlagTextReset(FlagReadoutItem.OrangeCircleFlag)
            assertEquals("オレンジボールフラッグ、車両に不具合があります", vm.uiState.first().flagText(FlagReadoutItem.OrangeCircleFlag))
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "") }
            coVerify(
                exactly = 1,
            ) { texts.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "オレンジボールフラッグ、車両に不具合があります") }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            confirmVerified(texts)
        }

    @Test
    fun `RedYellowStripesの文言を監視し空白保存とリセットを反映する`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap())
            val textFlow = MutableStateFlow("完走")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) } returns textFlow
            every { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) } returns flowOf("ホワイトフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) } returns flowOf("グリーンフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.RED) } returns flowOf("レッドフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) } returns flowOf("ブルーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) } returns flowOf("ブラックフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカーフラッグ")
            every { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) } returns flowOf("ブラック・ホワイトフラッグ")
            coEvery { tts.isAvailable() } returns true
            coEvery { texts.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "") } answers
                { textFlow.update { "" } }
            coEvery {
                texts.saveText(
                    AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES,
                    "レッド・イエローストライプフラッグ、路面が滑りやすいです",
                )
            } answers
                {
                    textFlow.update { "レッド・イエローストライプフラッグ、路面が滑りやすいです" }
                }
            val vm = createViewModel()
            assertEquals(
                FlagReadoutItem.entries.associateWith {
                    if (it == FlagReadoutItem.RedYellowStripesFlag) "完走" else it.defaultText
                },
                vm.uiState.first().flagTexts,
            )
            assertTrue(vm.uiState.first().isTextToSpeechAvailable)
            vm.onFlagTextChanged(FlagReadoutItem.RedYellowStripesFlag, "   ")
            assertFalse(vm.uiState.first().hasReadoutText(FlagReadoutItem.RedYellowStripesFlag))
            vm.onFlagTextReset(FlagReadoutItem.RedYellowStripesFlag)
            assertEquals(
                "レッド・イエローストライプフラッグ、路面が滑りやすいです",
                vm.uiState.first().flagText(FlagReadoutItem.RedYellowStripesFlag),
            )
            coVerify(exactly = 1) { texts.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "") }
            coVerify(exactly = 1) {
                texts.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "レッド・イエローストライプフラッグ、路面が滑りやすいです")
            }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.WHITE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.GREEN) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.RED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLUE) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            verify(exactly = 1) { texts.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE) }
            confirmVerified(texts)
        }
}
