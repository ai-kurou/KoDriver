package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.isManualReadoutHotkeyVirtualKeyCode
import kurou.kodriver.domain.model.manualReadoutItemKeys
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyVirtualKeyCodeUseCase
import kurou.kodriver.domain.usecase.RetryManualReadoutHotkeyRegistrationUseCase
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
) : ViewModel() {
    private val availableItemKeys = manualReadoutItemKeys(simulatorId)
    private val deleted = MutableStateFlow(false)
    private val hotkeyConflict = MutableStateFlow<ManualReadoutHotkeyConflict?>(null)
    private val mutationMutex = Mutex()

    val uiState =
        combine(
            observeSets(),
            deleted,
            hotkeyConflict,
            observeDebugHotkeyVirtualKeyCode(),
            observeHotkeyRegistrationState(simulatorId, setId),
        ) { sets, isDeleted, conflict, debugVirtualKeyCode, registration ->
            val index = sets.indexOfFirst { it.id == setId }
            val hotkey = sets.getOrNull(index)?.hotkeyVirtualKeyCode
            // 保存後にデバッグ用ホットキーが同じキーへ変更された場合も警告する。
            val savedConflict =
                ManualReadoutHotkeyConflict.DebugHotkey.takeIf { hotkey != null && hotkey == debugVirtualKeyCode }
            ManualReadoutSetDetailUiState(
                setNumber = if (index >= 0) index + 1 else null,
                availableItemKeys = availableItemKeys,
                selectedItemKeys = sets.getOrNull(index)?.itemKeys.orEmpty(),
                itemTexts = sets.getOrNull(index)?.itemTexts.orEmpty(),
                hotkeyVirtualKeyCode = hotkey,
                hotkeyRegistrationState = registration,
                hotkeyConflict = conflict ?: savedConflict,
                isLoading = false,
                deleted = isDeleted,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ManualReadoutSetDetailUiState(availableItemKeys = availableItemKeys),
        )

    fun toggleItem(itemKey: String) {
        if (itemKey !in availableItemKeys) return
        viewModelScope.launch {
            mutationMutex.withLock {
                if (deleted.value) return@withLock
                val set = observeSets().first().find { it.id == setId } ?: return@withLock
                val keys =
                    if (itemKey in set.itemKeys) set.itemKeys.filterNot { it == itemKey } else set.itemKeys + itemKey
                updateSet(set.copy(itemKeys = keys))
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
                    findOtherSetConflict(sets, virtualKeyCode)
                        ?: ManualReadoutHotkeyConflict.DebugHotkey.takeIf {
                            observeDebugHotkeyVirtualKeyCode().first() == virtualKeyCode
                        }
                if (conflict == null) updateSet(set.copy(hotkeyVirtualKeyCode = virtualKeyCode))
                hotkeyConflict.update { conflict }
            }
        }
    }

    // OS のホットキーはシミュレーターをまたいで共有されるため、全シミュレーターのセットと重複を確認する。
    private suspend fun findOtherSetConflict(
        sets: List<ManualReadoutSet>,
        virtualKeyCode: Int,
    ): ManualReadoutHotkeyConflict.OtherSet? {
        val ownIndex = sets.indexOfFirst { it.id != setId && it.hotkeyVirtualKeyCode == virtualKeyCode }
        if (ownIndex >= 0) return ManualReadoutHotkeyConflict.OtherSet(simulatorId, ownIndex + 1)
        observeOtherSimulatorSets.forEach { (otherSimulatorId, observeOtherSets) ->
            val index = observeOtherSets().first().indexOfFirst { it.hotkeyVirtualKeyCode == virtualKeyCode }
            if (index >= 0) return ManualReadoutHotkeyConflict.OtherSet(otherSimulatorId, index + 1)
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
