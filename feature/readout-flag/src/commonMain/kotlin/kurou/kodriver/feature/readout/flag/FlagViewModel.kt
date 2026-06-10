package kurou.kodriver.feature.readout.flag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveFlagEnabledStateUseCase

private val flagKeys = listOf(
    ReadoutItemKey.BLUE_FLAG,
    ReadoutItemKey.SECTOR_YELLOW_FLAG,
    ReadoutItemKey.FULL_COURSE_YELLOW,
    ReadoutItemKey.RED_FLAG,
)

internal class FlagViewModel(
    observeFlagEnabledStates: ObserveFlagEnabledStatesUseCase,
    private val saveFlagEnabledState: SaveFlagEnabledStateUseCase,
) : ViewModel() {

    val uiState: StateFlow<FlagUiState> = observeFlagEnabledStates()
        .map { storedStates ->
            val enabledStates = flagKeys.associateWith { storedStates[it] ?: true }
            FlagUiState(enabledStates = enabledStates)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FlagUiState())

    fun onFlagEnabledChanged(key: String, enabled: Boolean) {
        viewModelScope.launch { saveFlagEnabledState(key, enabled) }
    }
}
