package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsVehicleApproachEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 車両接近アナウンス詳細設定（ace-windows-readout-vehicle-approach-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutVehicleApproachDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsVehicleApproachPreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.AceWindows.id) の PlaySpeechEventUseCase（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutVehicleApproachDetailModule =
    module {
        // ViewModel（get(named(Simulator.AceWindows.id)) は narrator モジュールの PlaySpeechEventUseCase を解決）
        viewModel {
            AceWindowsReadoutVehicleApproachDetailViewModel(
                get(),
                get(),
                get(),
                get(named(Simulator.AceWindows.id)),
            )
        }

        factoryOf(::AceWindowsVehicleApproachThresholdsUseCases)
        factoryOf(::ObserveAceWindowsVehicleApproachEnabledStatesUseCase)
        factoryOf(::SaveAceWindowsVehicleApproachEnabledStateUseCase)
    }
