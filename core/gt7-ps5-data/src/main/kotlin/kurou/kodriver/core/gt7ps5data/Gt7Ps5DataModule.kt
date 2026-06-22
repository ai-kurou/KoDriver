package kurou.kodriver.core.gt7ps5data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kurou.kodriver.core.gt7ps5data.datasource.ConsoleAddressPreferences
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5PacketSource
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpSource
import kurou.kodriver.core.gt7ps5data.datasource.createConsoleAddressDataStore
import kurou.kodriver.core.gt7ps5data.repository.ConsoleAddressRepositoryImpl
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5RepositoryImpl
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val GT7_PS5_SCOPE_QUALIFIER = "gt7_ps5_scope"

val gt7Ps5DataModule = module {
    single(named(GT7_PS5_SCOPE_QUALIFIER)) { CoroutineScope(SupervisorJob()) }
    single<DataStore<ConsoleAddressPreferences>> {
        createConsoleAddressDataStore("${System.getProperty("user.home")}/.kodriver")
    }
    single<ConsoleAddressRepository> { ConsoleAddressRepositoryImpl(get()) }
    single<Gt7Ps5PacketSource> {
        Gt7Ps5UdpSource(
            consoleAddressFlow = get<ConsoleAddressRepository>().consoleAddress(),
            scope = get(named(GT7_PS5_SCOPE_QUALIFIER)),
        )
    }
    single<Gt7Ps5Repository> { Gt7Ps5RepositoryImpl(source = get()) }
}
