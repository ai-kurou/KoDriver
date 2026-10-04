package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * タイヤ残存率アナウンス詳細設定（lmu-windows-readout-tyre-wear-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutTyreWearDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: LmuWindowsTyreWearPreferencesRepository・ReadoutPreferencesRepository・
 *   TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.LmuWindows.id) の PlaySpeechEventUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutTyreWearDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutTyreWearDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            TyreWearReadoutUseCases(
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveLmuWindowsTyreWearReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsTyreWearReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveLmuWindowsTyreWearThresholdPercentageUseCase)
        factoryOf(::SaveLmuWindowsTyreWearThresholdPercentageUseCase)
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
        factory { TyreWearUseCases(get(), get(), get(), get(), get(), get()) }
    }
