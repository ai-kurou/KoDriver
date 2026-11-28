package kurou.kodriver.feature.otherwakewordhotkeydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.isHotkeyVirtualKeyCode
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.usecase.ObserveWakeWordHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ObserveWakeWordHotkeyUseCase
import kurou.kodriver.domain.speechrecognition.usecase.RetryWakeWordHotkeyRegistrationUseCase
import kurou.kodriver.domain.speechrecognition.usecase.SaveWakeWordHotkeyUseCase

internal class OtherWakeWordHotkeyDetailViewModel(
    private val observeHotkey: ObserveWakeWordHotkeyUseCase,
    private val saveHotkey: SaveWakeWordHotkeyUseCase,
    observeRegistrationState: ObserveWakeWordHotkeyRegistrationStateUseCase,
    private val retryRegistration: RetryWakeWordHotkeyRegistrationUseCase,
    private val observeManualReadoutSets: Map<String, ObserveManualReadoutSetsUseCase>,
) : ViewModel() {
    private val conflict = MutableStateFlow<OtherWakeWordHotkeyConflict?>(null)

    // 保存済みの割り当ての取得→変更→保存を直列化し、連続操作による上書きを防ぐ。
    private val mutationMutex = Mutex()

    val uiState =
        combine(observeHotkey(), conflict, observeRegistrationState()) { hotkey, conflict, registration ->
            OtherWakeWordHotkeyDetailUiState(
                hotkeyVirtualKeyCode = hotkey.virtualKeyCode,
                hotkeyModifiers = hotkey.modifiers,
                hotkeyConflict = conflict,
                hotkeyRegistrationState = registration,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OtherWakeWordHotkeyDetailUiState(),
        )

    fun assignHotkey(virtualKeyCode: Int) {
        if (!isHotkeyVirtualKeyCode(virtualKeyCode)) return
        changeHotkey { it.copy(virtualKeyCode = virtualKeyCode) }
    }

    fun selectHotkeyModifiers(modifiers: ManualReadoutHotkeyModifiers) {
        changeHotkey { it.copy(modifiers = modifiers) }
    }

    fun clearHotkey() {
        changeHotkey { it.copy(virtualKeyCode = null) }
    }

    fun onRetryHotkeyRegistration() {
        viewModelScope.launchCapturingErrors { retryRegistration() }
    }

    private fun changeHotkey(transform: (WakeWordHotkey) -> WakeWordHotkey) {
        viewModelScope.launchCapturingErrors {
            mutationMutex.withLock {
                val current = observeHotkey().first()
                val updated = transform(current)
                if (updated == current) {
                    conflict.update { null }
                    return@withLock
                }
                val found = updated.virtualKeyCode?.let { findConflict(it, updated.modifiers) }
                if (found == null) saveHotkey(updated)
                conflict.update { found }
            }
        }
    }

    // OS のホットキーはアプリ全体で共有されるため、全シミュレーターの手動読み上げセットと重複を確認する。
    private suspend fun findConflict(
        virtualKeyCode: Int,
        modifiers: ManualReadoutHotkeyModifiers,
    ): OtherWakeWordHotkeyConflict? {
        observeManualReadoutSets.forEach { (simulatorId, observeSets) ->
            val sets = observeSets().first()
            val index =
                sets.indexOfFirst {
                    it.hotkeyVirtualKeyCode == virtualKeyCode && it.hotkeyModifiers == modifiers
                }
            if (index >= 0) {
                return OtherWakeWordHotkeyConflict(
                    simulatorId,
                    sets[index].number.takeIf { it > 0 } ?: (index + 1),
                    sets[index].name,
                )
            }
        }
        return null
    }
}
