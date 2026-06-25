package kurou.kodriver.core.gt7ps5data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5PacketSource
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpSource
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5RepositoryImpl
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val GT7_PS5_SCOPE_QUALIFIER = "gt7_ps5_scope"

val gt7Ps5DataModule = module {
    single(named(GT7_PS5_SCOPE_QUALIFIER)) { CoroutineScope(SupervisorJob()) }
    single<Gt7Ps5PacketSource> {
        Gt7Ps5UdpSource(
            consoleAddressFlow = get<ConsoleAddressRepository>().consoleAddress(),
            listenPortFlow = get<Gt7UdpPortPreferencesRepository>().port(),
            scope = get(named(GT7_PS5_SCOPE_QUALIFIER)),
        )
    }
    single<Gt7Ps5Repository> { Gt7Ps5RepositoryImpl(source = get()) }
}
