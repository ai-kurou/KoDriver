package kurou.kodriver.feature.readout.vehicledamage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class VehicleDamageViewModel : ViewModel() {

    val uiState: StateFlow<VehicleDamageUiState> = MutableStateFlow(VehicleDamageUiState()).asStateFlow()
}
