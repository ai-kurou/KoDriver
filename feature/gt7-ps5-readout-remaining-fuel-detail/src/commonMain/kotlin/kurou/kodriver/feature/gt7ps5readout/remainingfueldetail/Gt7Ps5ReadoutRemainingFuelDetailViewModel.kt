package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelThresholdPercentageUseCase

internal class Gt7Ps5ReadoutRemainingFuelDetailViewModel(
    observeThresholdPercentage: ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase,
    private val saveThresholdPercentage: SaveGt7Ps5RemainingFuelThresholdPercentageUseCase,
) : ViewModel() {

    val uiState: StateFlow<Gt7Ps5ReadoutRemainingFuelDetailUiState> =
        observeThresholdPercentage()
            .map { Gt7Ps5ReadoutRemainingFuelDetailUiState(thresholdPercentage = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                Gt7Ps5ReadoutRemainingFuelDetailUiState(),
            )

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        onThresholdChanged(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
    }
}
