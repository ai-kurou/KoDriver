package kurou.kodriver.feature.otherfeedbackdetail

import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kurou.kodriver.domain.usecase.SendFeedbackUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * フィードバック送信詳細（other-feedback-detail feature）の Koin モジュール。
 *
 * 消費（get で解決）: TelemetryLogRepository（:core:data で登録）。
 */
val otherFeedbackDetailModule =
    module {
        viewModelOf(::OtherFeedbackDetailViewModel)

        factory { SendFeedbackUseCase(get()) }
        factory { ObserveTelemetryLogDetailUseCase(get()) }
    }
