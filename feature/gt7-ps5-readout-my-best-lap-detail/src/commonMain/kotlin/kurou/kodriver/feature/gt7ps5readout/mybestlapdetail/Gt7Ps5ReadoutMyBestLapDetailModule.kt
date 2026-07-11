package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapVoiceTypeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 自己ベストラップアナウンス詳細設定（gt7-ps5-readout-my-best-lap-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ReadoutMyBestLapDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: Gt7Ps5MyBestLapPreferencesRepository（:core:data）、試聴用の
 *   named("gt7_ps5") の TextToSpeechEngine（:feature:gt7-ps5-narrator で登録）。
 */
val gt7Ps5ReadoutMyBestLapDetailModule = module {
    // ViewModel（get(named "gt7_ps5") は narrator モジュールの TextToSpeechEngine を解決）
    viewModel { Gt7Ps5ReadoutMyBestLapDetailViewModel(get(), get(), get(named("gt7_ps5"))) }

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factory { ObserveGt7Ps5MyBestLapVoiceTypeUseCase(get()) }
    factory { SaveGt7Ps5MyBestLapVoiceTypeUseCase(get()) }
}
