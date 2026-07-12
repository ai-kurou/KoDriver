package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:data の代わりに main 系 Repository の
 * Fake 実装をバインドする。AppUpdateRepository は GitHub への実ネットワークアクセスを、
 * 他の2つは実DataStore（`~/.kodriver`）への書き込みを避けるために用意している。
 */
val fakeMainModule = module {
    single<AppUpdateRepository> { FakeAppUpdateRepository() }
    single<ExitConfirmationEnabledRepository> { FakeExitConfirmationEnabledRepository() }
    single<KeepScreenOnEnabledRepository> { FakeKeepScreenOnEnabledRepository() }
}

class FakeAppUpdateRepository : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppUpdate? = null
}

class FakeExitConfirmationEnabledRepository : ExitConfirmationEnabledRepository {
    private val flow = MutableStateFlow(true)
    override fun exitConfirmationEnabled(): Flow<Boolean> = flow
    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) { flow.update { enabled } }
}

class FakeKeepScreenOnEnabledRepository : KeepScreenOnEnabledRepository {
    private val flow = MutableStateFlow(false)
    override fun keepScreenOn(): Flow<Boolean> = flow
    override suspend fun saveKeepScreenOn(enabled: Boolean) { flow.update { enabled } }
}
