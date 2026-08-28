package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsMyBestLapVoiceTypeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACE 自己ベストラップアナウンス詳細設定（ace-windows-readout-my-best-lap-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutMyBestLapDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsMyBestLapPreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.AceWindows.id) の TextToSpeechEngine（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutMyBestLapDetailModule =
    module {
        // ViewModel（get(named(Simulator.AceWindows.id)) は narrator モジュールの TextToSpeechEngine を解決）
        viewModel { AceWindowsReadoutMyBestLapDetailViewModel(get(), get(), get(named(Simulator.AceWindows.id))) }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveAceWindowsMyBestLapVoiceTypeUseCase(get()) }
        factory { SaveAceWindowsMyBestLapVoiceTypeUseCase(get()) }
    }
