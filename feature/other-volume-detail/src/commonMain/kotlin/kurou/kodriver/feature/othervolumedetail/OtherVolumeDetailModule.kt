package kurou.kodriver.feature.othervolumedetail

import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 音量設定詳細（other-volume-detail feature）の Koin モジュール。
 *
 * 提供: OtherVolumeDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: SoundVolumePreferencesRepository（:core:data で登録）。
 */
val otherVolumeDetailModule =
    module {
        // ViewModel
        viewModelOf(::OtherVolumeDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { SaveSoundVolumeUseCase(get()) }
    }
