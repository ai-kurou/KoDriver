package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:data の代わりに main 系 Repository の
 * Fake 実装をバインドする。AppUpdateRepository は GitHub への実ネットワークアクセスを、
 * KeepScreenOnEnabledRepository は実DataStore（`~/.kodriver`）への書き込みを避けるために用意している。
 * WakeWordHotkeyRepository は、OS へのホットキー登録を避けるために用意している。
 */
val fakeMainModule =
    module {
        single<AppUpdateRepository> { FakeAppUpdateRepository() }
        single<KeepScreenOnEnabledRepository> { FakeKeepScreenOnEnabledRepository() }
        single<WakeWordHotkeyRepository> { FakeMainWakeWordHotkeyRepository() }
    }

class FakeAppUpdateRepository : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppUpdate? = null
}

class FakeKeepScreenOnEnabledRepository : KeepScreenOnEnabledRepository {
    private val flow = MutableStateFlow(false)

    override fun keepScreenOn(): Flow<Boolean> = flow

    override suspend fun saveKeepScreenOn(enabled: Boolean) {
        flow.update { enabled }
    }
}

class FakeMainWakeWordHotkeyRepository : WakeWordHotkeyRepository {
    override fun pressStream(): Flow<Unit> = emptyFlow()

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override suspend fun retryRegistration() = Unit
}
