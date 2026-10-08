package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ピットタイミングアナウンス詳細設定（lmu-windows-readout-pit-timing-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutPitTimingDetailViewModel と、予想残り周回数・有効状態・両ソースの文言の Observe/Save UseCase。
 * UseCase が依存する設定・文言の Repository は :core:data の
 * desktopDataModule / androidDataModule で束ねられる。試聴用の named(Simulator.LmuWindows.id) の
 * PlayStartSoundForKeyUseCase・SpeakTextUseCase は :feature:lmu-windows-narrator で登録される。
 * 消費: TextToSpeechRepository・SoundVolumePreferencesRepository と既存のピットタイミング設定Repository。
 */
val lmuWindowsReadoutPitTimingDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutPitTimingDetailViewModel(
                get(),
                get(),
            )
        }
        factory {
            PitTimingReadoutUseCases(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase)
        factoryOf(::ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase)
        factoryOf(::ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase)
        factoryOf(::ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factory { PitTimingUseCases(get(), get(), get(), get(), get(), get()) }
        factoryOf(::ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase)
        factoryOf(::ObserveLmuWindowsPitTimingTyreWearLapsUseCase)
        factoryOf(::ObserveLmuWindowsPitTimingEnabledStatesUseCase)
        factoryOf(::SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase)
        factoryOf(::SaveLmuWindowsPitTimingTyreWearLapsUseCase)
        factoryOf(::SaveLmuWindowsPitTimingEnabledStateUseCase)
    }
