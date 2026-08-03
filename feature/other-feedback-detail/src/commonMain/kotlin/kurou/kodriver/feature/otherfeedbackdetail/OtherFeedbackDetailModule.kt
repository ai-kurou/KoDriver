package kurou.kodriver.feature.otherfeedbackdetail

import kurou.kodriver.domain.usecase.SendFeedbackUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * フィードバック送信詳細（other-feedback-detail feature）の Koin モジュール。
 */
val otherFeedbackDetailModule =
    module {
        viewModelOf(::OtherFeedbackDetailViewModel)

        factory { SendFeedbackUseCase(get()) }
    }
