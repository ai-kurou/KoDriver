package kurou.kodriver.core.gt7ps5data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5AddressPreferences
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5PacketSource
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpSource
import kurou.kodriver.core.gt7ps5data.datasource.createGt7Ps5AddressDataStore
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5AddressRepositoryImpl
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5RepositoryImpl
import kurou.kodriver.domain.repository.Gt7Ps5AddressRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val GT7_PS5_SCOPE_QUALIFIER = "gt7_ps5_scope"

val gt7Ps5DataModule = module {
    single(named(GT7_PS5_SCOPE_QUALIFIER)) { CoroutineScope(SupervisorJob()) }
    single<DataStore<Gt7Ps5AddressPreferences>> {
        createGt7Ps5AddressDataStore("${System.getProperty("user.home")}/.kodriver")
    }
    single<Gt7Ps5AddressRepository> { Gt7Ps5AddressRepositoryImpl(get()) }
    single<Gt7Ps5PacketSource> {
        Gt7Ps5UdpSource(
            ps5AddressFlow = get<Gt7Ps5AddressRepository>().gt7Ps5Address(),
            scope = get(named(GT7_PS5_SCOPE_QUALIFIER)),
        )
    }
    single<Gt7Ps5Repository> { Gt7Ps5RepositoryImpl(source = get()) }
}
