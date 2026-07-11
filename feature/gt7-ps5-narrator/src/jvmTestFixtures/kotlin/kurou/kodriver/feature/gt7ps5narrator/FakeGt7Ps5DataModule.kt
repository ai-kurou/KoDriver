package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:data の代わりに GT7 系 Repository の
 * Fake 実装をバインドし、narrator の ViewModel/UseCase を Koin 経由で解決するテストで使う。
 */
val fakeGt7Ps5DataModule = module {
    single<Gt7Ps5Repository> { FakeGt7Ps5Repository() }
    single<Gt7Ps5UdpPortPreferencesRepository> { FakeGt7Ps5UdpPortPreferencesRepository() }
}

private class FakeGt7Ps5Repository : Gt7Ps5Repository {
    override fun telemetryStream(): Flow<Gt7Ps5TelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
}

private class FakeGt7Ps5UdpPortPreferencesRepository : Gt7Ps5UdpPortPreferencesRepository {
    private val flow = MutableStateFlow(33740)

    override fun port(): Flow<Int> = flow
    override suspend fun savePort(port: Int) { flow.update { port } }
}
