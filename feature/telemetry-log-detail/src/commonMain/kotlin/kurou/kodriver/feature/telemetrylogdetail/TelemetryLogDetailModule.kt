package kurou.kodriver.feature.telemetrylogdetail

import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * テレメトリログ詳細（telemetry-log-detail feature）の Koin モジュール。
 *
 * 提供: TelemetryLogDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: TelemetryLogRepository（:core:data で登録）。
 */
val telemetryLogDetailModule =
    module {
        // ViewModel
        viewModelOf(::TelemetryLogDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の TelemetryLogRepository を解決）
        factory { ObserveTelemetryLogDetailUseCase(get()) }
    }
