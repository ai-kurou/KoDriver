package kurou.kodriver.data

import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.koin.dsl.module

val dataModule = module {
    single<SimulatorPreferencesRepository> {
        createSimulatorPreferencesRepository(
            directory = "${System.getProperty("user.home")}/.kodriver",
        )
    }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { SaveSelectedSimulatorUseCase(get()) }
}
