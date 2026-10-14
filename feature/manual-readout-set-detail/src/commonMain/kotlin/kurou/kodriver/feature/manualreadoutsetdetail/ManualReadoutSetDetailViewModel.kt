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
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.manualReadoutItemKeys
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.UpdateManualReadoutSetUseCase

internal class ManualReadoutSetDetailViewModel(
    simulatorId: String,
    private val setId: String,
    private val observeSets: ObserveManualReadoutSetsUseCase,
    private val updateSet: UpdateManualReadoutSetUseCase,
    private val deleteSet: DeleteManualReadoutSetUseCase,
) : ViewModel() {
    private val availableItemKeys = manualReadoutItemKeys(simulatorId)
    private val deleted = MutableStateFlow(false)
    private val mutationMutex = Mutex()

    val uiState =
        combine(observeSets(), deleted) { sets, isDeleted ->
            val index = sets.indexOfFirst { it.id == setId }
            ManualReadoutSetDetailUiState(
                setNumber = if (index >= 0) index + 1 else null,
                availableItemKeys = availableItemKeys,
                selectedItemKeys = sets.getOrNull(index)?.itemKeys.orEmpty(),
                itemTexts = sets.getOrNull(index)?.itemTexts.orEmpty(),
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
