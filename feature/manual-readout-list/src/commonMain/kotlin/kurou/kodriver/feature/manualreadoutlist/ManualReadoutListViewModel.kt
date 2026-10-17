package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.AddManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
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
    private val deleteManualReadoutSet: DeleteManualReadoutSetUseCase,
    private val generateSetId: () -> String = { Uuid.random().toString() },
) : ViewModel() {
    private val canReadSets =
        simulatorId == Simulator.LmuWindows.id ||
            simulatorId == Simulator.Gt7Ps5.id ||
            simulatorId == Simulator.AceWindows.id

    private val selectedSetId = MutableStateFlow<String?>(null)

    val uiState =
        combine(observeManualReadoutSets(), selectedSetId) { sets, selectedId ->
            ManualReadoutListUiState(
                sets = sets,
                canReadSets = canReadSets,
                selectedSetId = selectedId?.takeIf { id -> sets.any { it.id == id } },
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ManualReadoutListUiState(canReadSets = canReadSets),
        )

    fun selectSet(setId: String) {
        selectedSetId.value = setId
    }

    fun clearSelectedSet() {
        selectedSetId.value = null
    }

    fun readSet(setId: String) {
        viewModelScope.launch { requestManualReadout(ManualReadoutRequest(simulatorId, setId)) }
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch { deleteManualReadoutSet(setId) }
    }

    fun addSet() {
        viewModelScope.launch { addManualReadoutSet(ManualReadoutSet(id = generateSetId())) }
    }
}
