package kurou.kodriver.feature.othervoicedetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class OtherVoiceDetailViewModel : ViewModel() {
    val uiState: StateFlow<OtherVoiceDetailUiState> = MutableStateFlow(OtherVoiceDetailUiState).asStateFlow()
}
