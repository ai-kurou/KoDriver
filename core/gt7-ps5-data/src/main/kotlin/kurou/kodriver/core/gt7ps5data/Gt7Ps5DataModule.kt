package kurou.kodriver.core.gt7ps5data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpSource
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5RepositoryImpl
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val GT7_PS5_ADDRESS_QUALIFIER = "gt7_ps5_address"
private const val GT7_PS5_SCOPE_QUALIFIER = "gt7_ps5_scope"

val gt7Ps5DataModule = module {
    single(named(GT7_PS5_ADDRESS_QUALIFIER)) { "" }
    single(named(GT7_PS5_SCOPE_QUALIFIER)) { CoroutineScope(SupervisorJob()) }
    single {
        Gt7Ps5UdpSource(
            ps5Address = get(named(GT7_PS5_ADDRESS_QUALIFIER)),
            scope = get(named(GT7_PS5_SCOPE_QUALIFIER)),
        )
    }
    single<Gt7Ps5Repository> {
        val address: String = get(named(GT7_PS5_ADDRESS_QUALIFIER))
        if (address.isNotBlank()) {
            Gt7Ps5RepositoryImpl(source = get())
        } else {
            NoOpGt7Ps5Repository()
        }
    }
}

private class NoOpGt7Ps5Repository : Gt7Ps5Repository {
    override fun telemetryStream(): Flow<Gt7Ps5TelemetryData> = emptyFlow()
}
