package kurou.kodriver.feature.othervoicespeeddetail

import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SaveVoiceSpeedUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 読み上げ速度設定詳細（other-voice-speed-detail feature）の Koin モジュール。
 *
 * 提供: OtherVoiceSpeedDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: VoiceSpeedPreferencesRepository（:core:data で登録）。
 */
val otherVoiceSpeedDetailModule =
    module {
        viewModelOf(::OtherVoiceSpeedDetailViewModel)

        factory { ObserveVoiceSpeedUseCase(get()) }
        factory { SaveVoiceSpeedUseCase(get()) }
    }
