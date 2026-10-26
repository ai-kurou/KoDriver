package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.usecase.AddManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutPlayButtonLayoutEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutUseCase
import kurou.kodriver.domain.usecase.SaveManualReadoutPlayButtonLayoutEnabledUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal data class PlayButtonLayoutUseCases(
    val observe: ObserveManualReadoutPlayButtonLayoutEnabledUseCase,
    val save: SaveManualReadoutPlayButtonLayoutEnabledUseCase,
)

/** 選択中シミュレーターの保存済みセット一覧を観測し、セットの追加と読み上げ依頼を行う。 */
@OptIn(ExperimentalUuidApi::class)
internal class ManualReadoutListViewModel(
    private val simulatorId: String,
    private val requestManualReadout: RequestManualReadoutUseCase,
    observeManualReadoutSets: ObserveManualReadoutSetsUseCase,
    private val addManualReadoutSet: AddManualReadoutSetUseCase,
    private val deleteManualReadoutSet: DeleteManualReadoutSetUseCase,
    private val playButtonLayoutUseCases: PlayButtonLayoutUseCases,
    private val generateSetId: () -> String = { Uuid.random().toString() },
) : ViewModel() {
    private val selectedSetId = MutableStateFlow<String?>(null)

    private val listFlow =
        ManualReadoutListFlow(
            sets = observeManualReadoutSets(),
            selectedSetId = selectedSetId,
            playButtonLayoutEnabled = playButtonLayoutUseCases.observe(),
            simulatorId = simulatorId,
        )

    val uiState =
        listFlow.uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            listFlow.initialUiState,
        )

    fun selectSet(setId: String) {
        selectedSetId.value = setId
    }

    fun onPlayButtonLayoutToggle() {
        viewModelScope.launch { playButtonLayoutUseCases.save(!uiState.value.playButtonLayoutEnabled) }
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
