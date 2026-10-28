package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase

internal class OtherReadoutStartSoundDetailViewModel(
    observeReadoutStartSoundType: ObserveReadoutStartSoundTypeUseCase,
    private val saveReadoutStartSoundType: SaveReadoutStartSoundTypeUseCase,
    private val previewStartSound: PreviewStartSoundUseCase,
) : ViewModel() {
    private val pendingType = MutableStateFlow<ReadoutStartSoundType?>(null)

    val uiState =
        OtherReadoutStartSoundDetailFlow(
            savedType = observeReadoutStartSoundType(),
            pendingType = pendingType,
        ).uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherReadoutStartSoundDetailUiState())

    fun onPendingTypeSelected(type: ReadoutStartSoundType) {
        pendingType.update { type }
        previewStartSound(type)
    }

    fun onConfirm() {
        val type = pendingType.value ?: return
        viewModelScope.launch { saveReadoutStartSoundType(type) }
        pendingType.update { null }
    }

    fun onDismiss() {
        pendingType.update { null }
    }
}
