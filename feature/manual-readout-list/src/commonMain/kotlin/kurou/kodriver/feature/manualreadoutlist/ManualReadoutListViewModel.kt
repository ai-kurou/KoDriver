package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase

/** 選択中シミュレーターの保存済みセット一覧を観測する。 */
internal class ManualReadoutListViewModel(
    observeManualReadoutSets: ObserveManualReadoutSetsUseCase,
) : ViewModel() {
    val uiState =
        observeManualReadoutSets()
            .map { ManualReadoutListUiState(sets = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManualReadoutListUiState())
}
