package kurou.kodriver.feature.otherconsoleipdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:data の代わりに
 * ConsoleAddressPreferencesRepository の Fake 実装をバインドし、実DataStore
 * （`~/.kodriver`）への書き込みを避ける。
 */
val fakeOtherConsoleIpDetailModule = module {
    single<ConsoleAddressPreferencesRepository> { FakeConsoleAddressPreferencesRepository() }
}

class FakeConsoleAddressPreferencesRepository : ConsoleAddressPreferencesRepository {
    private val flow = MutableStateFlow<String?>(null)

    override fun consoleAddress(): Flow<String?> = flow

    override suspend fun saveConsoleAddress(address: String) {
        flow.update { address }
    }
}
