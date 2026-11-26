package kurou.kodriver.feature.otherfeedbackdetail

import kurou.kodriver.domain.telemetrylog.usecase.ObserveTelemetryLogDetailUseCase
import kurou.kodriver.domain.usecase.CanSendFeedbackUseCase
import kurou.kodriver.domain.usecase.SendFeedbackUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * フィードバック送信詳細（other-feedback-detail feature）の Koin モジュール。
 *
 * 消費（get で解決）: TelemetryLogRepository・FeedbackCooldownPreferencesRepository（:core:data で登録）。
 */
public val otherFeedbackDetailModule: Module =
    module {
        viewModelOf(::OtherFeedbackDetailViewModel)

        factory { SendFeedbackUseCase(get(), get()) }
        factory { CanSendFeedbackUseCase(get()) }
        factory { ObserveTelemetryLogDetailUseCase(get()) }
    }
