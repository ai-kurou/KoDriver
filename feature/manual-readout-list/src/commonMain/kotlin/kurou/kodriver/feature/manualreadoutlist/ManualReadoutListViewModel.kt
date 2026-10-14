package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.usecase.AddManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** 選択中シミュレーターの保存済みセット一覧を観測し、空のセットを追加する。 */
@OptIn(ExperimentalUuidApi::class)
internal class ManualReadoutListViewModel(
    observeManualReadoutSets: ObserveManualReadoutSetsUseCase,
    private val addManualReadoutSet: AddManualReadoutSetUseCase,
    private val generateSetId: () -> String = { Uuid.random().toString() },
) : ViewModel() {
    val uiState =
        observeManualReadoutSets()
            .map { ManualReadoutListUiState(sets = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManualReadoutListUiState())

    fun addSet() {
        viewModelScope.launch { addManualReadoutSet(ManualReadoutSet(id = generateSetId())) }
    }
}
