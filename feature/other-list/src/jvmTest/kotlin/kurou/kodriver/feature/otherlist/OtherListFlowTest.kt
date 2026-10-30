package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechUnavailableReasonUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListFlowTest {
    private val repository: TextToSpeechRepository = mockk()
    private val tts = OtherListTtsAvailabilityFlow(CheckTextToSpeechUnavailableReasonUseCase(repository))
    private val baseState = MutableStateFlow(OtherListUiState())
    private val overlaySettings = MutableStateFlow(OtherListOverlaySettingsUiState())
    private val deviceSettings = MutableStateFlow(OtherListDeviceSettingsUiState())
    private val speechSettings = MutableStateFlow(OtherListSpeechSettingsUiState())
    private val connection = MutableStateFlow(OtherListConnectionUiState())
    private val deviceVolume = MutableStateFlow(0)
    private val reset = MutableStateFlow(OtherListResetUiState())
    private val autoReadoutSettingsReset = MutableStateFlow(OtherListResetUiState())
    private val manualReadoutSettingsReset = MutableStateFlow(OtherListResetUiState())
    private val startupRegistration = MutableStateFlow(OtherListStartupRegistrationUiState())
    private val appUpdate = MutableStateFlow(OtherListAppUpdateUiState())
    private var activeCollections = 0
    private var collectionStarts = 0

    @Test
    fun `初期状態を合成し各入力の更新とローカル状態を保持する`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherListUiState(), flow.uiState.first())
            val expected =
                OtherListUiState(
                    selectedItem = OtherListItemType.Feedback,
                    selectedFeedbackTelemetryLogId = 42,
                    feedbackAttachRequestId = 3,
                    appVersion = "1.2.3",
                    appVersionLabel = "Windows",
                    accessLocalNetworkPermissionGranted = false,
                    overlayVisible = !overlaySettings.value.overlayVisible,
                    overlayTextSize = OverlayTextSize.entries.first { it != overlaySettings.value.overlayTextSize },
                    overlayTextColor = 0x123456,
                    overlayBackgroundOpacity = 25,
                    keepScreenOn = !deviceSettings.value.keepScreenOn,
                    dynamicColorEnabled = !deviceSettings.value.dynamicColorEnabled,
                    hapticFeedbackEnabled = !deviceSettings.value.hapticFeedbackEnabled,
                    themeMode = ThemeMode.entries.first { it != deviceSettings.value.themeMode },
                    voiceId = "voice",
                    voiceSpeed = 1.5f,
                    voicePitch = 0.8f,
                    androidAudioUsage = AndroidAudioUsage.COMMUNICATION,
                    readoutStartSoundType = ReadoutStartSoundType.ELECTRONIC_NOISE,
                    soundVolume = 40,
                    deviceVolume = 70,
                    serverIp = "192.168.1.2",
                    consoleAddress = "192.168.1.3",
                    consolePort = 33741,
                    reset = OtherListResetUiState(showResetConfirmDialog = true),
                    autoReadoutSettingsReset = OtherListResetUiState(resetSucceeded = false),
                    manualReadoutSettingsReset = OtherListResetUiState(showResetConfirmDialog = true),
                    startupEnabled = true,
                    hasAppUpdate = true,
                )
            val updated = async(UnconfinedTestDispatcher(testScheduler)) { flow.uiState.first { it == expected } }

            baseState.update {
                it.copy(
                    selectedItem = expected.selectedItem,
                    selectedFeedbackTelemetryLogId = expected.selectedFeedbackTelemetryLogId,
                    feedbackAttachRequestId = expected.feedbackAttachRequestId,
                    appVersion = expected.appVersion,
                    appVersionLabel = expected.appVersionLabel,
                    accessLocalNetworkPermissionGranted = false,
                )
            }
            overlaySettings.update {
                OtherListOverlaySettingsUiState(
                    expected.overlayVisible,
                    expected.overlayTextSize,
                    expected.overlayTextColor,
                    expected.overlayBackgroundOpacity,
                )
            }
            deviceSettings.update {
                OtherListDeviceSettingsUiState(
                    expected.keepScreenOn,
                    expected.dynamicColorEnabled,
                    expected.hapticFeedbackEnabled,
                    expected.themeMode,
                )
            }
            speechSettings.update {
                OtherListSpeechSettingsUiState(
                    expected.voiceId,
                    expected.voiceSpeed,
                    expected.voicePitch,
                    expected.androidAudioUsage,
                    expected.readoutStartSoundType,
                    expected.soundVolume,
                )
            }
            connection.update {
                OtherListConnectionUiState(
                    expected.serverIp,
                    expected.consoleAddress,
                    expected.consolePort,
                )
            }
            deviceVolume.update { expected.deviceVolume }
            reset.update { expected.reset }
            autoReadoutSettingsReset.update { expected.autoReadoutSettingsReset }
            manualReadoutSettingsReset.update { expected.manualReadoutSettingsReset }
            startupRegistration.update { OtherListStartupRegistrationUiState(true) }
            appUpdate.update { OtherListAppUpdateUiState(true) }

            assertEquals(expected, updated.await())
        }

    @Test
    fun `TTS案内を差し替えて順序を整え利用可能時には除去する`() =
        runTest {
            val items = listOf(OtherListItemType.KeepScreenOn, OtherListItemType.TtsLanguageDataMissing)
            baseState.update { it.copy(items = items) }
            coEvery { repository.unavailableReason() } returns TextToSpeechUnavailableReason.EngineMissing
            val flow = createFlow()
            val updated =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first { it.ttsUnavailableGuidance == TtsUnavailableGuidance.EngineMissing }
                }

            tts.checkAvailability()

            assertEquals(
                listOf(OtherListItemType.KeepScreenOn, OtherListItemType.TtsEngineMissing).sortedBy { it.ordinal },
                updated.await().items,
            )
            coEvery { repository.unavailableReason() } returns null
            tts.checkAvailability()
            val available = flow.uiState.first()
            assertEquals(listOf(OtherListItemType.KeepScreenOn), available.items)
            assertEquals(null, available.ttsUnavailableGuidance)
            coVerify(exactly = 2) { repository.unavailableReason() }
            confirmVerified(repository)
        }

    @Test
    fun `購読前には収集せず購読解除で停止し再購読で最新入力を反映する`() =
        runTest {
            val flow = createFlow()
            assertEquals(0, collectionStarts)
            assertEquals(0, activeCollections)

            assertEquals(0, flow.uiState.first().deviceVolume)
            assertEquals(1, collectionStarts)
            assertEquals(0, activeCollections)
            deviceVolume.update { 80 }
            baseState.update { it.copy(selectedItem = OtherListItemType.Feedback) }

            val state = flow.uiState.first()
            assertEquals(80, state.deviceVolume)
            assertEquals(OtherListItemType.Feedback, state.selectedItem)
            assertEquals(2, collectionStarts)
            assertEquals(0, activeCollections)
        }

    private fun createFlow() =
        OtherListFlow(
            baseState =
                flow {
                    collectionStarts++
                    activeCollections++
                    try {
                        baseState.collect { emit(it) }
                    } finally {
                        activeCollections--
                    }
                },
            overlaySettings = overlaySettings,
            deviceSettings = deviceSettings,
            speechSettings = speechSettings,
            connection = connection,
            deviceVolume = deviceVolume,
            ttsAvailability = tts.uiState,
            applyGuidance = tts::applyGuidance,
            reset = reset,
            autoReadoutSettingsReset = autoReadoutSettingsReset,
            manualReadoutSettingsReset = manualReadoutSettingsReset,
            startupRegistration = startupRegistration,
            appUpdate = appUpdate,
        )
}
