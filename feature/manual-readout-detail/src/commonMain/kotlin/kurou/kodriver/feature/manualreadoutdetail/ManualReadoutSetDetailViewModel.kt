package kurou.kodriver.feature.manualreadoutdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.isManualReadoutHotkeyVirtualKeyCode
import kurou.kodriver.domain.model.takeManualReadoutSetName
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyVirtualKeyCodeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.RetryManualReadoutHotkeyRegistrationUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kurou.kodriver.domain.usecase.UpdateManualReadoutSetUseCase

@Suppress("LongParameterList")
internal class ManualReadoutSetDetailViewModel(
    private val simulatorId: String,
    private val setId: String,
    private val observeSets: ObserveManualReadoutSetsUseCase,
    private val updateSet: UpdateManualReadoutSetUseCase,
    private val deleteSet: DeleteManualReadoutSetUseCase,
    private val observeDebugHotkeyVirtualKeyCode: ObserveReadoutHotkeyVirtualKeyCodeUseCase,
    private val observeOtherSimulatorSets: Map<String, ObserveManualReadoutSetsUseCase>,
    observeHotkeyRegistrationState: ObserveManualReadoutHotkeyRegistrationStateUseCase,
    private val retryHotkeyRegistration: RetryManualReadoutHotkeyRegistrationUseCase,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    observeSoundVolume: ObserveSoundVolumeUseCase,
    playStartSoundForKey: PlayStartSoundForKeyUseCase,
    speakText: SpeakTextUseCase,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            checkTextToSpeechAvailable,
            observeSoundVolume,
            playStartSoundForKey,
            speakText,
        )

    private val availableItemKeys = manualReadoutItemKeys(simulatorId)
    private val deleted = MutableStateFlow(false)
    private val hotkeyConflict = MutableStateFlow<ManualReadoutHotkeyConflict?>(null)

    // セット全体の取得→変更→保存を直列化し、項目・文言・キーの連続操作による上書きや削除後の更新を防ぐ。
    private val mutationMutex = Mutex()

    private val detailFlow =
        ManualReadoutSetDetailFlow(
            sets = observeSets(),
            deleted = deleted,
            hotkeyConflict = hotkeyConflict,
            debugHotkeyVirtualKeyCode = observeDebugHotkeyVirtualKeyCode(),
            hotkeyRegistrationState = observeHotkeyRegistrationState(simulatorId, setId),
            textToSpeechAvailable = preview.textToSpeechAvailable,
            isPreviewing = preview.isPreviewing,
            simulatorId = simulatorId,
            setId = setId,
            availableItemKeys = availableItemKeys,
        )

    val uiState =
        detailFlow.uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            detailFlow.initialUiState,
        )

    fun onPreviewStopped() = preview.stop()

    fun onPreviewClicked() {
        val state = uiState.value
        val key =
            state.selectedItemKeys.firstOrNull()?.let {
                if (it == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value ||
                    it == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value
                ) {
                    LmuWindowsReadoutItemKey.PitTiming.Root
                } else if (it == LmuWindowsReadoutItemKey.RemainingFuel.Laps.value) {
                    LmuWindowsReadoutItemKey.RemainingFuel.Root
                } else {
                    ReadoutItemKey.fromValue(it)
                }
            } ?: return
        preview.onPreviewClicked(manualReadoutPreview(state), key)
    }

    fun addItem(itemKey: String) {
        if (itemKey !in availableItemKeys) return
        mutateItems { keys ->
            if (itemKey in keys || keys.size >= MANUAL_READOUT_SET_ITEM_MAX) keys else keys + itemKey
        }
    }

    // 保存済みで選べなくなった項目も上限枠を空けられるよう、対応項目かどうかは問わない。
    fun removeItem(itemKey: String) {
        mutateItems { keys -> keys.filterNot { it == itemKey } }
    }

    fun moveItem(
        from: Int,
        to: Int,
    ) {
        mutateItems { keys ->
            if (from !in keys.indices || to !in keys.indices || from == to) {
                keys
            } else {
                keys.toMutableList().apply { add(to, removeAt(from)) }
            }
        }
    }

    private fun mutateItems(transform: (List<String>) -> List<String>) {
        viewModelScope.launch {
            mutationMutex.withLock {
                if (deleted.value) return@withLock
                val set = observeSets().first().find { it.id == setId } ?: return@withLock
                val keys = transform(set.itemKeys)
                if (keys != set.itemKeys) updateSet(set.copy(itemKeys = keys))
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                if (deleted.value) return@withLock
                val set = observeSets().first().find { it.id == setId } ?: return@withLock
                updateSet(set.copy(name = name.trim().takeManualReadoutSetName()))
            }
        }
    }

    fun updateItemText(
        itemKey: String,
        text: String,
    ) {
        if (itemKey !in availableItemKeys) return
        viewModelScope.launch {
            mutationMutex.withLock {
                if (deleted.value) return@withLock
                val set = observeSets().first().find { it.id == setId } ?: return@withLock
                val normalizedText = text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
                updateSet(set.copy(itemTexts = set.itemTexts + (itemKey to normalizedText)))
            }
        }
    }

    fun assignHotkey(virtualKeyCode: Int) {
        if (!isManualReadoutHotkeyVirtualKeyCode(virtualKeyCode)) return
        viewModelScope.launch {
            mutationMutex.withLock {
                if (deleted.value) return@withLock
                val sets = observeSets().first()
                val set = sets.find { it.id == setId } ?: return@withLock
                if (set.hotkeyVirtualKeyCode == virtualKeyCode) {
                    hotkeyConflict.update { null }
                    return@withLock
                }
                val conflict =
                    findOtherSetConflict(sets, virtualKeyCode, set.hotkeyModifiers)
                        ?: ManualReadoutHotkeyConflict.DebugHotkey.takeIf {
                            set.hotkeyModifiers == ManualReadoutHotkeyModifiers.CTRL_ALT_SHIFT &&
                                observeDebugHotkeyVirtualKeyCode().first() == virtualKeyCode
                        }
                if (conflict == null) updateSet(set.copy(hotkeyVirtualKeyCode = virtualKeyCode))
                hotkeyConflict.update { conflict }
            }
        }
    }

    fun selectHotkeyModifiers(modifiers: ManualReadoutHotkeyModifiers) {
        viewModelScope.launch {
            mutationMutex.withLock {
                if (deleted.value) return@withLock
                val sets = observeSets().first()
                val set = sets.find { it.id == setId } ?: return@withLock
                if (set.hotkeyModifiers == modifiers) {
                    hotkeyConflict.update { null }
                    return@withLock
                }
                val key = set.hotkeyVirtualKeyCode
                val conflict =
                    key?.let {
                        findOtherSetConflict(sets, it, modifiers)
                            ?: ManualReadoutHotkeyConflict.DebugHotkey.takeIf {
                                modifiers == ManualReadoutHotkeyModifiers.CTRL_ALT_SHIFT &&
                                    observeDebugHotkeyVirtualKeyCode().first() == key
                            }
                    }
                if (conflict == null) updateSet(set.copy(hotkeyModifiers = modifiers))
                hotkeyConflict.update { conflict }
            }
        }
    }

    // OS のホットキーはシミュレーターをまたいで共有されるため、全シミュレーターのセットと重複を確認する。
    private suspend fun findOtherSetConflict(
        sets: List<ManualReadoutSet>,
        virtualKeyCode: Int,
        modifiers: ManualReadoutHotkeyModifiers,
    ): ManualReadoutHotkeyConflict.OtherSet? {
        val ownIndex =
            sets.indexOfFirst {
                it.id != setId && it.hotkeyVirtualKeyCode == virtualKeyCode && it.hotkeyModifiers == modifiers
            }
        if (ownIndex >= 0) {
            return ManualReadoutHotkeyConflict.OtherSet(
                simulatorId,
                sets[ownIndex].number.takeIf { it > 0 } ?: (ownIndex + 1),
                sets[ownIndex].name,
            )
        }
        observeOtherSimulatorSets.forEach { (otherSimulatorId, observeOtherSets) ->
            val otherSets = observeOtherSets().first()
            val index =
                otherSets.indexOfFirst {
                    it.hotkeyVirtualKeyCode == virtualKeyCode && it.hotkeyModifiers == modifiers
                }
            if (index >= 0) {
                return ManualReadoutHotkeyConflict.OtherSet(
                    otherSimulatorId,
                    otherSets[index].number.takeIf { it > 0 } ?: (index + 1),
                    otherSets[index].name,
                )
            }
        }
        return null
    }

    fun onRetryHotkeyRegistration() {
        viewModelScope.launch { retryHotkeyRegistration(simulatorId, setId) }
    }

    fun clearHotkey() {
        viewModelScope.launch {
            mutationMutex.withLock {
                if (deleted.value) return@withLock
                val set = observeSets().first().find { it.id == setId } ?: return@withLock
                updateSet(set.copy(hotkeyVirtualKeyCode = null))
                hotkeyConflict.update { null }
            }
        }
    }

    fun deleteSet() {
        viewModelScope.launch {
            mutationMutex.withLock {
                if (!deleted.value) {
                    deleteSet(setId)
                    deleted.update { true }
                }
            }
        }
    }
}
