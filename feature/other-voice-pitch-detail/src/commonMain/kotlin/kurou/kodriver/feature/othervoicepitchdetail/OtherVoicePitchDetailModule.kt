package kurou.kodriver.feature.othervoicepitchdetail

import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.SaveVoicePitchUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 声の高さ設定詳細（other-voice-pitch-detail feature）の Koin モジュール。
 *
 * 提供: OtherVoicePitchDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: VoicePitchPreferencesRepository（:core:data で登録）。
 */
val otherVoicePitchDetailModule =
    module {
        viewModelOf(::OtherVoicePitchDetailViewModel)

        factory { ObserveVoicePitchUseCase(get()) }
        factory { SaveVoicePitchUseCase(get()) }
    }
