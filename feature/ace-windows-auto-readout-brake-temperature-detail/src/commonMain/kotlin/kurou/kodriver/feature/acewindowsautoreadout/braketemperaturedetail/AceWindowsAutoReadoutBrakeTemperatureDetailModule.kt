package kurou.kodriver.feature.acewindowsautoreadout.braketemperaturedetail

import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBrakeTemperatureEnabledStateUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ブレーキ温度アナウンス詳細設定（ace-windows-auto-readout-brake-temperature-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsAutoReadoutBrakeTemperatureDetailViewModel、UseCase 集約 data class とドメイン UseCase。
 * 消費（get で解決）: AceWindowsBrakeTemperaturePreferencesRepository・TextToSpeechRepository・
 *   SoundVolumePreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.AceWindows.id) のTTS・開始音UseCase（:feature:ace-windows-narrator で登録）。
 */
public val aceWindowsAutoReadoutBrakeTemperatureDetailModule: Module =
    module {
        // 試聴用TTS・開始音は BrakeTemperatureReadoutUseCases 経由で解決する。
        viewModel {
            AceWindowsAutoReadoutBrakeTemperatureDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            BrakeTemperatureReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase)
        factoryOf(::SaveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factory { BrakeTemperatureUseCases(get(), get(), get(), get()) }
        factoryOf(::ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase)
        factoryOf(::ObserveAceWindowsBrakeTemperatureHighThresholdUseCase)
        factoryOf(::SaveAceWindowsBrakeTemperatureEnabledStateUseCase)
        factoryOf(::SaveAceWindowsBrakeTemperatureHighThresholdUseCase)
    }
