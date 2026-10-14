package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.AddManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** 選択中シミュレーターの保存済みセット一覧を観測し、セットの追加と読み上げ依頼を行う。 */
@OptIn(ExperimentalUuidApi::class)
internal class ManualReadoutListViewModel(
    private val simulatorId: String,
    private val requestManualReadout: RequestManualReadoutUseCase,
    observeManualReadoutSets: ObserveManualReadoutSetsUseCase,
    private val addManualReadoutSet: AddManualReadoutSetUseCase,
    private val generateSetId: () -> String = { Uuid.random().toString() },
) : ViewModel() {
    private val canReadSets = simulatorId == Simulator.LmuWindows.id

    val uiState =
        observeManualReadoutSets()
            .map { ManualReadoutListUiState(sets = it, canReadSets = canReadSets) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                ManualReadoutListUiState(canReadSets = canReadSets),
            )

    fun readSet(setId: String) {
        viewModelScope.launch { requestManualReadout(ManualReadoutRequest(simulatorId, setId)) }
    }

    fun addSet() {
        viewModelScope.launch { addManualReadoutSet(ManualReadoutSet(id = generateSetId())) }
    }
}
