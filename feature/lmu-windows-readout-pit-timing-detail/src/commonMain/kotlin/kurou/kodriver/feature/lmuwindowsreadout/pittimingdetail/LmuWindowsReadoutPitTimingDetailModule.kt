package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ピットタイミングアナウンス詳細設定（lmu-windows-readout-pit-timing-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutPitTimingDetailViewModel と、予想残り周回数・有効状態の Observe/Save UseCase。
 * UseCase が依存する LmuWindowsPitTimingPreferencesRepository は :core:data の
 * desktopDataModule / androidDataModule で束ねられる。試聴用の named(Simulator.LmuWindows.id) の
 * PlaySpeechEventUseCase は :feature:lmu-windows-narrator で登録される。
 */
val lmuWindowsReadoutPitTimingDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutPitTimingDetailViewModel(
                get(),
                get(named(Simulator.LmuWindows.id)),
            )
        }
        factory { PitTimingUseCases(get(), get(), get(), get(), get(), get()) }
        factoryOf(::ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase)
        factoryOf(::ObserveLmuWindowsPitTimingTyreWearLapsUseCase)
        factoryOf(::ObserveLmuWindowsPitTimingEnabledStatesUseCase)
        factoryOf(::SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase)
        factoryOf(::SaveLmuWindowsPitTimingTyreWearLapsUseCase)
        factoryOf(::SaveLmuWindowsPitTimingEnabledStateUseCase)
    }
