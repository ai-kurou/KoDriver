package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class LmuWindowsReadoutTyreTemperatureDetailViewModel : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> =
        MutableStateFlow(LmuWindowsReadoutTyreTemperatureDetailUiState()).asStateFlow()
}
