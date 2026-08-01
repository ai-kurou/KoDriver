package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapVoiceTypeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * LMU 自己ベストラップアナウンス詳細設定（lmu-windows-readout-my-best-lap-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutMyBestLapDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsMyBestLapPreferencesRepository（:core:data）、試聴用の
 *   named("lmu_windows") の TextToSpeechEngine（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutMyBestLapDetailModule =
    module {
    // ViewModel（get(named "lmu_windows") は narrator モジュールの TextToSpeechEngine を解決）
    viewModel { LmuWindowsReadoutMyBestLapDetailViewModel(get(), get(), get(named("lmu_windows"))) }

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factory { ObserveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
    factory { SaveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
}
