package kurou.kodriver.feature.readout.vehicleapproach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.usecase.ObserveProximityUseCase

internal class VehicleApproachViewModel(
    observeProximity: ObserveProximityUseCase,
) : ViewModel() {

    val uiState: StateFlow<VehicleApproachUiState> = observeProximity()
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VehicleApproachUiState())

    private fun ProximityData.toUiState() = VehicleApproachUiState(
        isSideBySideLeft = isSideBySideLeft,
        isSideBySideRight = isSideBySideRight,
        lateralDistanceLeftMeters = lateralDistanceLeftMeters.takeIf { it != Double.MAX_VALUE },
        lateralDistanceRightMeters = lateralDistanceRightMeters.takeIf { it != Double.MAX_VALUE },
    )
}
