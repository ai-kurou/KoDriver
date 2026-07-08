package kurou.kodriver.feature.otherthemedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase

class OtherThemeDetailViewModel(
    observeThemeMode: ObserveThemeModeUseCase,
    private val saveThemeMode: SaveThemeModeUseCase,
) : ViewModel() {

    private val pendingThemeMode = MutableStateFlow<ThemeMode?>(null)

    val uiState = combine(observeThemeMode(), pendingThemeMode) { saved, pending ->
        OtherThemeDetailUiState(
            selectedThemeMode = saved,
            pendingThemeMode = pending ?: saved,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherThemeDetailUiState())

    fun onPendingThemeModeSelected(themeMode: ThemeMode) {
        pendingThemeMode.update { themeMode }
    }

    fun onConfirm() {
        val themeMode = pendingThemeMode.value ?: return
        viewModelScope.launch { saveThemeMode(themeMode) }
        pendingThemeMode.update { null }
    }

    fun onDismiss() {
        pendingThemeMode.update { null }
    }
}
