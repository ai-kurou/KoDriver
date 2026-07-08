package kurou.kodriver.feature.telemetryloglist

import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val telemetryLogListModule = module {
    factory { ObserveTelemetryLogsUseCase(get()) }
    factory { ResetTelemetryLogDatabaseUseCase(get()) }
    viewModelOf(::TelemetryLogListViewModel)
}
