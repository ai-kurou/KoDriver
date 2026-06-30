package kurou.kodriver.feature.telemetryloglist

import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val telemetryLogListModule = module {
    factory { ObserveTelemetryLogsUseCase(get()) }
    viewModelOf(::TelemetryLogListViewModel)
}
