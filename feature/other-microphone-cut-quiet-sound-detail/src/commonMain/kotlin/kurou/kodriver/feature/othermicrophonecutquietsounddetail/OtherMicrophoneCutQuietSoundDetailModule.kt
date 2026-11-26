package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import kurou.kodriver.domain.microphone.usecase.ObserveMicrophoneCutQuietSoundInCommandUseCase
import kurou.kodriver.domain.microphone.usecase.ObserveMicrophoneCutQuietSoundLevelUseCase
import kurou.kodriver.domain.microphone.usecase.SaveMicrophoneCutQuietSoundInCommandUseCase
import kurou.kodriver.domain.microphone.usecase.SaveMicrophoneCutQuietSoundLevelUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 小さい音をカット設定詳細（other-microphone-cut-quiet-sound-detail feature）の Koin モジュール。
 *
 * 提供: OtherMicrophoneCutQuietSoundDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: MicrophoneCutQuietSoundPreferencesRepository（:core:data で登録）。
 */
public val otherMicrophoneCutQuietSoundDetailModule: Module =
    module {
        viewModelOf(::OtherMicrophoneCutQuietSoundDetailViewModel)

        factory { ObserveMicrophoneCutQuietSoundLevelUseCase(get()) }
        factory { SaveMicrophoneCutQuietSoundLevelUseCase(get()) }
        factory { ObserveMicrophoneCutQuietSoundInCommandUseCase(get()) }
        factory { SaveMicrophoneCutQuietSoundInCommandUseCase(get()) }
    }
