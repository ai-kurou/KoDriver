package kurou.kodriver.feature.telemetryloglist

import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * テレメトリログ一覧（telemetry-log-list feature）の Koin モジュール。
 *
 * 提供: TelemetryLogListViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: TelemetryLogRepository（:core:data で登録）。
 */
val telemetryLogListModule = module {
    // ViewModel
    viewModelOf(::TelemetryLogListViewModel)

    // ドメイン UseCase（:core:domain。get() は :core:data の TelemetryLogRepository を解決）
    factory { ObserveTelemetryLogsUseCase(get()) }
    factory { ResetTelemetryLogDatabaseUseCase(get()) }
}
