package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * タイヤ温度アナウンス詳細設定（ace-windows-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutTyreTemperatureDetailViewModel、UseCase 集約 data class とドメイン UseCase。
 * 消費（get で解決）: AceWindowsTyreTemperaturePreferencesRepository・TextToSpeechRepository・
 *   SoundVolumePreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.AceWindows.id) のTTS・開始音UseCase（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutTyreTemperatureDetailModule =
    module {
        // 試聴用TTS・開始音は TyreTemperatureReadoutUseCases 経由で解決する。
        viewModel {
            AceWindowsReadoutTyreTemperatureDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            TyreTemperatureReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase)
        factoryOf(::SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factory { TyreTemperatureUseCases(get(), get(), get(), get()) }
        factoryOf(::ObserveAceWindowsTyreTemperatureEnabledStatesUseCase)
        factoryOf(::ObserveAceWindowsTyreTemperatureHighThresholdUseCase)
        factoryOf(::SaveAceWindowsTyreTemperatureEnabledStateUseCase)
        factoryOf(::SaveAceWindowsTyreTemperatureHighThresholdUseCase)
    }
