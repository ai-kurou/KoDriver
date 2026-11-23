package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases

/** OS起動時自動起動の取得・変更と表示状態を管理する。実行のライフサイクルは呼び出し元に従う。 */
public class OtherListStartupRegistrationFlow(
    private val startupRegistration: StartupRegistrationUseCases,
) {
    public val uiState: StateFlow<OtherListStartupRegistrationUiState>
        field = MutableStateFlow(OtherListStartupRegistrationUiState())

    public suspend fun checkStartupEnabled() {
        val enabled = startupRegistration.getEnabled()
        uiState.update { it.copy(startupEnabled = enabled) }
    }

    public suspend fun onStartupEnabledChange(enabled: Boolean) {
        startupRegistration.setEnabled(enabled)
        uiState.update { it.copy(startupEnabled = enabled) }
    }
}
