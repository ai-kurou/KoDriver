package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 燃料残り周回数アナウンス詳細設定（gt7-ps5-readout-remaining-fuel-laps-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: Gt7Ps5RemainingFuelLapsPreferencesRepository（:core:data）、試聴用の
 *   named("gt7_ps5") の TextToSpeechEngine（:feature:gt7-ps5-narrator で登録）。
 */
val gt7Ps5ReadoutRemainingFuelLapsDetailModule =
    module {
    // ViewModel（get(named "gt7_ps5") は narrator モジュールの TextToSpeechEngine を解決）
    viewModel { Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(get(), get(), get(named("gt7_ps5"))) }

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factory { ObserveGt7Ps5RemainingFuelLapsUseCase(get()) }
    factory { SaveGt7Ps5RemainingFuelLapsUseCase(get()) }
}
