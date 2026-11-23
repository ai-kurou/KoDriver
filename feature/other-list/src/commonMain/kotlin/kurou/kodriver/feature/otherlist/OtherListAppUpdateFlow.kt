package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase

/** アプリ更新の確認と表示状態を管理する。実行のライフサイクルは呼び出し元に従う。 */
public class OtherListAppUpdateFlow(
    private val checkAppUpdateAvailable: CheckAppUpdateAvailableUseCase,
    private val currentVersion: String,
) {
    public val uiState: StateFlow<OtherListAppUpdateUiState>
        field = MutableStateFlow(OtherListAppUpdateUiState())
    private var updateCheckStarted = false

    public suspend fun checkUpdate() {
        if (currentVersion.isBlank() || updateCheckStarted) return
        updateCheckStarted = true
        val hasUpdate = checkAppUpdateAvailable(currentVersion)
        uiState.update { it.copy(hasAppUpdate = hasUpdate) }
    }
}
