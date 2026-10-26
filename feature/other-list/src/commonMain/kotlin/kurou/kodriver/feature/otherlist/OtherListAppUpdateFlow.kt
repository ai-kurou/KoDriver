package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase

/** アプリ更新の確認と表示状態を管理する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListAppUpdateFlow(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
) {
    private val _uiState = MutableStateFlow(OtherListAppUpdateUiState())
    val uiState: StateFlow<OtherListAppUpdateUiState> = _uiState.asStateFlow()
    private var updateCheckStarted = false

    suspend fun checkUpdate() {
        if (currentVersion.isBlank() || updateCheckStarted) return
        updateCheckStarted = true
        val hasUpdate = checkAppUpdateAvailable(currentVersion)
        _uiState.update { it.copy(hasAppUpdate = hasUpdate) }
    }
}
