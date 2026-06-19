package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase

class OtherReadoutStartSoundDetailViewModel(
    observeReadoutStartSoundType: ObserveReadoutStartSoundTypeUseCase,
    private val saveReadoutStartSoundType: SaveReadoutStartSoundTypeUseCase,
    private val previewStartSound: PreviewStartSoundUseCase,
) : ViewModel() {

    private val pendingType = MutableStateFlow<ReadoutStartSoundType?>(null)

    val uiState = combine(observeReadoutStartSoundType(), pendingType) { saved, pending ->
        OtherReadoutStartSoundDetailUiState(
            selectedType = saved,
            pendingType = pending ?: saved,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherReadoutStartSoundDetailUiState())

    fun onPendingTypeSelected(type: ReadoutStartSoundType) {
        pendingType.value = type
        previewStartSound(type)
    }

    fun onConfirm() {
        val type = pendingType.value ?: return
        viewModelScope.launch { saveReadoutStartSoundType(type) }
        pendingType.value = null
    }

    fun onDismiss() {
        pendingType.value = null
    }
}
