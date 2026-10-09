package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class LmuWindowsReadoutVehicleDamageDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val stopSpeech: StopSpeechUseCase = mockk()
    private val playSpeechEvent: PlaySpeechEventUseCase = mockk()
    private val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    private val overheatText = MutableStateFlow("オーバーヒート")
    private val partDetachedText = MutableStateFlow("部品脱落")
    private val tyreDetachedText = MutableStateFlow("タイヤ脱落")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutVehicleDamageDetailViewModel(
            VehicleDamageUseCases(
                observeEnabledStates = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repository),
                saveEnabledState = SaveLmuWindowsVehicleDamageEnabledStateUseCase(repository),
                observeOverheatReadoutText = ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(repository),
                saveOverheatReadoutText = SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(repository),
                observePartDetachedReadoutText =
                    ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(
                        repository,
                    ),
                savePartDetachedReadoutText = SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(repository),
                observeTyreDetachedReadoutText =
                    ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(
                        repository,
                    ),
                saveTyreDetachedReadoutText = SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(repository),
            ),
            VehicleDamageReadoutUseCases(playSpeechEvent, stopSpeech, checkAvailable, observeVolume),
        )

    private fun stubSettings(available: Boolean = true) {
        every { repository.observeEnabledStates() } returns enabledStates
        every { repository.observeOverheatReadoutText() } returns overheatText
        every { repository.observePartDetachedReadoutText() } returns partDetachedText
        every { repository.observeTyreDetachedReadoutText() } returns tyreDetachedText
        coEvery { checkAvailable() } returns available
    }

    private fun verifySettings() {
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeOverheatReadoutText() }
        verify(exactly = 1) { repository.observePartDetachedReadoutText() }
        verify(exactly = 1) { repository.observeTyreDetachedReadoutText() }
        coVerify(exactly = 1) { checkAvailable() }
    }

    @Test
    fun `初期状態と保存済みの文言および有効状態を反映する`() =
        runTest {
            stubSettings()
            val viewModel = createViewModel()
            assertEquals(
                LmuWindowsReadoutVehicleDamageDetailUiState(isTextToSpeechAvailable = true),
                viewModel.uiState.first(),
            )
            enabledStates.update {
                mapOf(
                    ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false,
                    ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached to false,
                    ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached to false,
                )
            }
            overheatText.update { "エンジン" }
            partDetachedText.update { "パーツ" }
            tyreDetachedText.update { "ホイール" }
            assertEquals(
                LmuWindowsReadoutVehicleDamageDetailUiState(
                    overheatEnabled = false,
                    partDetachedEnabled = false,
                    tyreDetachedEnabled = false,
                    overheatReadoutText = "エンジン",
                    partDetachedReadoutText = "パーツ",
                    tyreDetachedReadoutText = "ホイール",
                    isTextToSpeechAvailable = true,
                ),
                viewModel.uiState.first(),
            )
            verifySettings()
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `オーバーヒートスイッチを保存する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false) } answers {
                enabledStates.update { mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false) }
            }
            val viewModel = createViewModel()
            viewModel.onOverheatEnabledChanged(false)
            assertEquals(false, viewModel.uiState.first().overheatEnabled)
            verifySettings()
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)
            }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `オーバーヒート文言は正規化して保存し反映する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveOverheatReadoutText("自由文言") } answers { overheatText.update { "自由文言" } }
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextChanged(" 自由文言 ")
            assertEquals("自由文言", viewModel.uiState.first().overheatReadoutText)
            verifySettings()
            coVerify(exactly = 1) { repository.saveOverheatReadoutText("自由文言") }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `オーバーヒート試聴はスイッチOFFでも入力中の文言を解決済みイベントで再生する`() =
        runTest {
            stubSettings()
            enabledStates.update { mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false) }
            every { observeVolume() } returns MutableStateFlow(40)
            every { playSpeechEvent(SpeechEvent.LmuWindowsOverheating(resolvedText = "編集中")) } returns Unit
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("編集中")
            verifySettings()
            verify(exactly = 1) { observeVolume() }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsOverheating(resolvedText = "編集中")) }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `部品脱落スイッチを保存する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached, false) } answers
                {
                    enabledStates.update { mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached to false) }
                }
            val viewModel = createViewModel()
            viewModel.onPartDetachedEnabledChanged(false)
            assertEquals(false, viewModel.uiState.first().partDetachedEnabled)
            verifySettings()
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached, false)
            }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `部品脱落文言は正規化して保存し反映する`() =
        runTest {
            stubSettings()
            coEvery { repository.savePartDetachedReadoutText("自由文言") } answers { partDetachedText.update { "自由文言" } }
            val viewModel = createViewModel()
            viewModel.onPartDetachedReadoutTextChanged(" 自由文言 ")
            assertEquals("自由文言", viewModel.uiState.first().partDetachedReadoutText)
            verifySettings()
            coVerify(exactly = 1) { repository.savePartDetachedReadoutText("自由文言") }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `部品脱落試聴はスイッチOFFでも入力中の文言を解決済みイベントで再生する`() =
        runTest {
            stubSettings()
            enabledStates.update { mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached to false) }
            every { observeVolume() } returns MutableStateFlow(40)
            every { playSpeechEvent(SpeechEvent.LmuWindowsPartDetached(resolvedText = "編集中")) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPartDetachedReadoutTextPreviewClicked("編集中")
            verifySettings()
            verify(exactly = 1) { observeVolume() }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsPartDetached(resolvedText = "編集中")) }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `タイヤ脱落スイッチを保存する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached, false) } answers
                {
                    enabledStates.update { mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached to false) }
                }
            val viewModel = createViewModel()
            viewModel.onTyreDetachedEnabledChanged(false)
            assertEquals(false, viewModel.uiState.first().tyreDetachedEnabled)
            verifySettings()
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached, false)
            }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `タイヤ脱落文言は正規化して保存し反映する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveTyreDetachedReadoutText("自由文言") } answers { tyreDetachedText.update { "自由文言" } }
            val viewModel = createViewModel()
            viewModel.onTyreDetachedReadoutTextChanged(" 自由文言 ")
            assertEquals("自由文言", viewModel.uiState.first().tyreDetachedReadoutText)
            verifySettings()
            coVerify(exactly = 1) { repository.saveTyreDetachedReadoutText("自由文言") }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `タイヤ脱落試聴はスイッチOFFでも入力中の文言を解決済みイベントで再生する`() =
        runTest {
            stubSettings()
            enabledStates.update { mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached to false) }
            every { observeVolume() } returns MutableStateFlow(40)
            every { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = "編集中")) } returns Unit
            val viewModel = createViewModel()
            viewModel.onTyreDetachedReadoutTextPreviewClicked("編集中")
            verifySettings()
            verify(exactly = 1) { observeVolume() }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = "編集中")) }
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `空白と改行だけの文言では3イベントとも試聴しない`() =
        runTest {
            stubSettings(available = true)
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked(" ")
            viewModel.onPartDetachedReadoutTextPreviewClicked(" ")
            viewModel.onTyreDetachedReadoutTextPreviewClicked(" ")
            verifySettings()
            viewModel.onOverheatReadoutTextPreviewClicked("\t\n")
            viewModel.onPartDetachedReadoutTextPreviewClicked("\t\n")
            viewModel.onTyreDetachedReadoutTextPreviewClicked("\t\n")
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsOverheating(resolvedText = " ")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsPartDetached(resolvedText = " ")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = " ")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `TTS利用不可では3イベントとも試聴しない`() =
        runTest {
            stubSettings(available = false)
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("入力中")
            viewModel.onPartDetachedReadoutTextPreviewClicked("入力中")
            viewModel.onTyreDetachedReadoutTextPreviewClicked("入力中")
            assertEquals(false, viewModel.uiState.first().isTextToSpeechAvailable)
            verifySettings()
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsOverheating(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsPartDetached(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = "入力中")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `音量ゼロでは3イベントとも試聴しない`() =
        runTest {
            stubSettings(available = true)
            every { observeVolume() } returns MutableStateFlow(0)
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("入力中")
            viewModel.onPartDetachedReadoutTextPreviewClicked("入力中")
            viewModel.onTyreDetachedReadoutTextPreviewClicked("入力中")
            verifySettings()
            verify(exactly = 3) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsOverheating(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsPartDetached(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = "入力中")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `負の音量では3イベントとも試聴しない`() =
        runTest {
            stubSettings(available = true)
            every { observeVolume() } returns MutableStateFlow(-1)
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("入力中")
            viewModel.onPartDetachedReadoutTextPreviewClicked("入力中")
            viewModel.onTyreDetachedReadoutTextPreviewClicked("入力中")
            verifySettings()
            verify(exactly = 3) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsOverheating(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsPartDetached(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = "入力中")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `ペインを離れると開始した試聴を一度だけ停止する`() =
        runTest {
            stubSettings()
            enabledStates.update { mapOf(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached to false) }
            every { observeVolume() } returns MutableStateFlow(40)
            every { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = "編集中")) } returns Unit
            every { stopSpeech() } returns Unit
            val viewModel = createViewModel()
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech() }
            viewModel.onTyreDetachedReadoutTextPreviewClicked("編集中")
            verifySettings()
            verify(exactly = 1) { observeVolume() }
            verify(exactly = 1) { playSpeechEvent(SpeechEvent.LmuWindowsTyreDetached(resolvedText = "編集中")) }
            viewModel.onPreviewStopped()
            viewModel.onPreviewStopped()
            verify(exactly = 1) { stopSpeech() }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }
}
