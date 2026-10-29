package kurou.kodriver.feature.lmuwindowsautoreadout.vehicledamagedetail

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
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class LmuWindowsAutoReadoutVehicleDamageDetailViewModelTest {
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
        LmuWindowsAutoReadoutVehicleDamageDetailViewModel(
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
                LmuWindowsAutoReadoutVehicleDamageDetailUiState(isTextToSpeechAvailable = true),
                viewModel.uiState.first(),
            )
            enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false,
                    LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to false,
                    LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to false,
                )
            }
            overheatText.update { "エンジン" }
            partDetachedText.update { "パーツ" }
            tyreDetachedText.update { "ホイール" }
            assertEquals(
                LmuWindowsAutoReadoutVehicleDamageDetailUiState(
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
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `3イベントのスイッチ状態を保存する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, false) } answers {
                enabledStates.update { it + (LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false) }
            }
            coEvery {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.PartDetached, false)
            } answers {
                enabledStates.update { it + (LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to false) }
            }
            coEvery {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached, false)
            } answers {
                enabledStates.update { it + (LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to false) }
            }
            val viewModel = createViewModel()
            viewModel.onOverheatEnabledChanged(false)
            viewModel.onPartDetachedEnabledChanged(false)
            viewModel.onTyreDetachedEnabledChanged(false)
            val uiState = viewModel.uiState.first()
            assertEquals(false, uiState.overheatEnabled)
            assertEquals(false, uiState.partDetachedEnabled)
            assertEquals(false, uiState.tyreDetachedEnabled)
            verifySettings()
            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.PartDetached, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached, false)
            }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `3イベントの文言を正規化して保存し反映する`() =
        runTest {
            stubSettings()
            coEvery { repository.saveOverheatReadoutText("自由文言") } answers { overheatText.update { "自由文言" } }
            coEvery { repository.savePartDetachedReadoutText("自由文言") } answers {
                partDetachedText.update { "自由文言" }
            }
            coEvery { repository.saveTyreDetachedReadoutText("自由文言") } answers { tyreDetachedText.update { "自由文言" } }
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextChanged(" 自由文言 ")
            viewModel.onPartDetachedReadoutTextChanged(" 自由文言 ")
            viewModel.onTyreDetachedReadoutTextChanged(" 自由文言 ")
            val uiState = viewModel.uiState.first()
            assertEquals("自由文言", uiState.overheatReadoutText)
            assertEquals("自由文言", uiState.partDetachedReadoutText)
            assertEquals("自由文言", uiState.tyreDetachedReadoutText)
            verifySettings()
            coVerify(exactly = 1) { repository.saveOverheatReadoutText("自由文言") }
            coVerify(exactly = 1) { repository.savePartDetachedReadoutText("自由文言") }
            coVerify(exactly = 1) { repository.saveTyreDetachedReadoutText("自由文言") }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
            confirmVerified(repository, checkAvailable, stopSpeech)
        }

    @Test
    fun `スイッチOFFでも3イベントの入力中文言を解決済みイベントで試聴する`() =
        runTest {
            stubSettings()
            enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false,
                    LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to false,
                    LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to false,
                )
            }
            every { observeVolume() } returns MutableStateFlow(40)
            every { playSpeechEvent(LmuWindowsOverheating(resolvedText = "編集中")) } returns Unit
            every { playSpeechEvent(LmuWindowsPartDetached(resolvedText = "編集中")) } returns Unit
            every { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = "編集中")) } returns Unit
            val viewModel = createViewModel()
            viewModel.onOverheatReadoutTextPreviewClicked("編集中")
            viewModel.onPartDetachedReadoutTextPreviewClicked("編集中")
            viewModel.onTyreDetachedReadoutTextPreviewClicked("編集中")
            verifySettings()
            verify(exactly = 3) { observeVolume() }
            verify(exactly = 1) { playSpeechEvent(LmuWindowsOverheating(resolvedText = "編集中")) }
            verify(exactly = 1) { playSpeechEvent(LmuWindowsPartDetached(resolvedText = "編集中")) }
            verify(exactly = 1) { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = "編集中")) }
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
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
            verify(exactly = 0) { playSpeechEvent(LmuWindowsOverheating(resolvedText = " ")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsPartDetached(resolvedText = " ")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = " ")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
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
            verify(exactly = 0) { playSpeechEvent(LmuWindowsOverheating(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsPartDetached(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = "入力中")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
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
            verify(exactly = 0) { playSpeechEvent(LmuWindowsOverheating(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsPartDetached(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = "入力中")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
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
            verify(exactly = 0) { playSpeechEvent(LmuWindowsOverheating(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsPartDetached(resolvedText = "入力中")) }
            verify(exactly = 0) { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = "入力中")) }
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }

    @Test
    fun `ペインを離れると開始した試聴を一度だけ停止する`() =
        runTest {
            stubSettings()
            enabledStates.update { mapOf(LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to false) }
            every { observeVolume() } returns MutableStateFlow(40)
            every { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = "編集中")) } returns Unit
            every { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) } returns Unit
            val viewModel = createViewModel()
            viewModel.onPreviewStopped()
            verify(exactly = 0) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
            viewModel.onTyreDetachedReadoutTextPreviewClicked("編集中")
            verifySettings()
            verify(exactly = 1) { observeVolume() }
            verify(exactly = 1) { playSpeechEvent(LmuWindowsTyreDetached(resolvedText = "編集中")) }
            viewModel.onPreviewStopped()
            viewModel.onPreviewStopped()
            verify(exactly = 1) { stopSpeech(LmuWindowsReadoutItemKey.VehicleDamage.Root) }
            confirmVerified(repository, checkAvailable, observeVolume, playSpeechEvent, stopSpeech)
        }
}
