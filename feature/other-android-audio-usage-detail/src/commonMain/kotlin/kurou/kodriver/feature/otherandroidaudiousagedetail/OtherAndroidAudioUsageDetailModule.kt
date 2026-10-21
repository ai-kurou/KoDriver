package kurou.kodriver.feature.otherandroidaudiousagedetail

import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.SaveAndroidAudioUsageUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 音声の用途設定詳細の Koin モジュール。
 * 提供: OtherAndroidAudioUsageDetailViewModel と監視・保存 UseCase。
 * 消費（get で解決）: AndroidAudioUsagePreferencesRepository（:core:data で登録）。
 */
val otherAndroidAudioUsageDetailModule =
    module {
        viewModelOf(::OtherAndroidAudioUsageDetailViewModel)

        factory { ObserveAndroidAudioUsageUseCase(get()) }
        factory { SaveAndroidAudioUsageUseCase(get()) }
    }
