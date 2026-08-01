package kurou.kodriver.feature.otherthemedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:data の代わりに ThemePreferencesRepository の
 * Fake 実装をバインドし、実DataStore（`~/.kodriver`）への書き込みを避ける。
 */
val fakeOtherThemeDetailModule =
    module {
    single<ThemePreferencesRepository> { FakeThemePreferencesRepository() }
}

class FakeThemePreferencesRepository : ThemePreferencesRepository {
    private val flow = MutableStateFlow(ThemeMode.SYSTEM)

    override fun observeThemeMode(): Flow<ThemeMode> = flow

    override suspend fun saveThemeMode(themeMode: ThemeMode) {
        flow.update { themeMode }
    }
}
