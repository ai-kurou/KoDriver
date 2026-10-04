package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 タイヤ温度アナウンス詳細設定（gt7-ps5-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ReadoutTyreTemperatureDetailViewModel。高温閾値・過熱警告の有効/無効・文言の永続化用 UseCase を解決する。
 * 試聴は gt7-ps5-narrator が提供する GT7 修飾子付き PlayStartSoundForKeyUseCase・SpeakTextUseCase を利用する。
 */
val gt7Ps5ReadoutTyreTemperatureDetailModule =
    module {
        viewModel {
            Gt7Ps5ReadoutTyreTemperatureDetailViewModel(get(), get())
        }

        factory {
            TyreTemperatureReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.Gt7Ps5.id)),
                get(named(Simulator.Gt7Ps5.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase)
        factoryOf(::SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factory { TyreTemperatureUseCases(get(), get(), get(), get()) }
        factoryOf(::ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase)
        factoryOf(::ObserveGt7Ps5TyreTemperatureHighThresholdUseCase)
        factoryOf(::SaveGt7Ps5TyreTemperatureEnabledStateUseCase)
        factoryOf(::SaveGt7Ps5TyreTemperatureHighThresholdUseCase)
    }
