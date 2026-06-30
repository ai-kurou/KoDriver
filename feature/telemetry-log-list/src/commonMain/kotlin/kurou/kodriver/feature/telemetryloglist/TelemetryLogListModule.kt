package kurou.kodriver.feature.telemetryloglist

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val telemetryLogListModule = module {
    viewModelOf(::TelemetryLogListViewModel)
}
