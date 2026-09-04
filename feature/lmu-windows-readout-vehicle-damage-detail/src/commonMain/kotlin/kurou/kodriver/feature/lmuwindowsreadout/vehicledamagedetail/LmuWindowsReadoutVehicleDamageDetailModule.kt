package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsOverheatVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsOverheatVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 車両故障アナウンス詳細設定（lmu-windows-readout-vehicle-damage-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutVehicleDamageDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsVehicleDamagePreferencesRepository・LmuWindowsOverheatPreferencesRepository
 *   （:core:data）、試聴用の named(Simulator.LmuWindows.id) の TextToSpeechEngine（:feature:lmu-windows-narrator
 *   で登録）。
 */
val lmuWindowsReadoutVehicleDamageDetailModule =
    module {
        // ViewModel（get(named(Simulator.LmuWindows.id)) は narrator モジュールの TextToSpeechEngine を解決）
        viewModel {
            LmuWindowsReadoutVehicleDamageDetailViewModel(
                get(),
                get(),
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsOverheatVoiceTypeUseCase(get()) }
        factory { SaveLmuWindowsVehicleDamageEnabledStateUseCase(get()) }
        factory { SaveLmuWindowsOverheatVoiceTypeUseCase(get()) }
    }
