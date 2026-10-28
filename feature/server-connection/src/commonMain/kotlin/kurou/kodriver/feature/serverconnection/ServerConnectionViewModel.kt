package kurou.kodriver.feature.serverconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.ObserveKoDriverServerConnectionUseCase

/**
 * ServerConnection 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class ServerConnectionViewModel(
    observeKoDriverServerConnection: ObserveKoDriverServerConnectionUseCase,
    private val appVersion: String,
) : ViewModel() {
    private val showVersionMismatchBottomSheetFlow = MutableStateFlow(false)
    private var versionMismatchWarningShown = false

    private val connectionStateFlow =
        observeKoDriverServerConnection(appVersion)
            .onEach { state ->
                if (state.isVersionMismatch && !versionMismatchWarningShown) {
                    versionMismatchWarningShown = true
                    showVersionMismatchBottomSheetFlow.update { true }
                }
            }.shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                replay = 1,
            )

    val uiState: StateFlow<ServerConnectionUiState> =
        ServerConnectionFlow(
            connectionState = connectionStateFlow,
            showVersionMismatchBottomSheet = showVersionMismatchBottomSheetFlow,
            appVersion = appVersion,
        ).uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ServerConnectionUiState(),
        )

    fun dismissVersionMismatchBottomSheet() {
        showVersionMismatchBottomSheetFlow.update { false }
    }
}
