package kurou.kodriver.feature.telemetrylogdetail

import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val telemetryLogDetailModule = module {
    factory { ObserveTelemetryLogDetailUseCase(get()) }
    viewModelOf(::TelemetryLogDetailViewModel)
}
