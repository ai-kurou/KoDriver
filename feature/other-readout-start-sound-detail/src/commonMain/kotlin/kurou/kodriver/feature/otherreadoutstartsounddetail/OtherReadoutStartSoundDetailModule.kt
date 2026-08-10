package kurou.kodriver.feature.otherreadoutstartsounddetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 読み上げ開始音設定詳細（other-readout-start-sound-detail feature）の Koin モジュール。
 *
 * 提供: OtherReadoutStartSoundDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: ReadoutStartSoundPreferencesRepository（:core:data）、および試聴用の
 *   named(Simulator.LmuWindows.id) の TextToSpeechEngine（:feature:lmu-windows-narrator で登録）。
 */
val otherReadoutStartSoundDetailModule =
    module {
        // ViewModel
        viewModelOf(::OtherReadoutStartSoundDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveReadoutStartSoundTypeUseCase(get()) }
        factory { SaveReadoutStartSoundTypeUseCase(get()) }

        // 試聴再生（named(Simulator.LmuWindows.id) の TextToSpeechEngine に依存）
        factory { PreviewStartSoundUseCase(get(named(Simulator.LmuWindows.id))) }
    }
