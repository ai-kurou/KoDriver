package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases

/** OS起動時自動起動の取得・変更と表示状態を管理する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListStartupRegistrationFlow(
    private val startupRegistration: StartupRegistrationUseCases,
) {
    private val _uiState = MutableStateFlow(OtherListStartupRegistrationUiState())
    val uiState: StateFlow<OtherListStartupRegistrationUiState> = _uiState.asStateFlow()

    suspend fun checkStartupEnabled() {
        val enabled = startupRegistration.getEnabled()
        _uiState.update { it.copy(startupEnabled = enabled) }
    }

    suspend fun onStartupEnabledChange(enabled: Boolean) {
        startupRegistration.setEnabled(enabled)
        _uiState.update { it.copy(startupEnabled = enabled) }
    }
}
