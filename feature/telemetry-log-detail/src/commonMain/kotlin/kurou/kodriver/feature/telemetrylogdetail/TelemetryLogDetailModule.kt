package kurou.kodriver.feature.telemetrylogdetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val telemetryLogDetailModule = module {
    viewModelOf(::TelemetryLogDetailViewModel)
}
