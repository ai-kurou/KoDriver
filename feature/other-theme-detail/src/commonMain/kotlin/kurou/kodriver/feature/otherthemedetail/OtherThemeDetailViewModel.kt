package kurou.kodriver.feature.otherthemedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase

/**
 * OtherThemeDetail 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class OtherThemeDetailViewModel internal constructor(
    observeThemeMode: ObserveThemeModeUseCase,
    private val saveThemeMode: SaveThemeModeUseCase,
) : ViewModel() {
    private var previewRequest = 0

    private val pendingThemeMode = MutableStateFlow<ThemeMode?>(null)

    internal val uiState =
        combine(observeThemeMode(), pendingThemeMode) { saved, pending ->
            OtherThemeDetailUiState(
                selectedThemeMode = saved,
                pendingThemeMode = pending ?: saved,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherThemeDetailUiState())

    internal fun onPendingThemeModeSelected(themeMode: ThemeMode) {
        previewRequest++
        pendingThemeMode.update { themeMode }
    }

    internal fun onConfirm() {
        val themeMode = pendingThemeMode.value ?: return
        val request = ++previewRequest
        viewModelScope.launch {
            try {
                saveThemeMode(themeMode)
                // 保存済み状態への反映を待ち、確定時に元のテーマが一瞬表示されるのを防ぐ。
                uiState.first { it.selectedThemeMode == themeMode }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 保存に失敗しても未保存のテーマを表示し続けず、保存済みの設定へ戻す。
            } finally {
                // 同じテーマを選び直した場合も、後続の選択・保存処理には触れない。
                if (previewRequest == request) pendingThemeMode.update { null }
            }
        }
    }

    internal fun onDismiss() {
        previewRequest++
        pendingThemeMode.update { null }
    }
}
