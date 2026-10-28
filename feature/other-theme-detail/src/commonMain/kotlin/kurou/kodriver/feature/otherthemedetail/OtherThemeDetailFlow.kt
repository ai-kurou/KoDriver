package kurou.kodriver.feature.otherthemedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.ThemeMode

/** 保存済みテーマと未保存の選択から画面状態を合成する冷たい Flow。 */
internal class OtherThemeDetailFlow(
    savedThemeMode: Flow<ThemeMode>,
    pendingThemeMode: Flow<ThemeMode?>,
) {
    val uiState: Flow<OtherThemeDetailUiState> =
        combine(savedThemeMode, pendingThemeMode) { saved, pending ->
            OtherThemeDetailUiState(
                selectedThemeMode = saved,
                pendingThemeMode = pending ?: saved,
            )
        }
}
