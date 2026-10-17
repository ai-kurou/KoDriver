package kurou.kodriver.feature.lmuwindowsautoreadout.pittimingdetail

import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingEnabledStateUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ピットタイミングアナウンス詳細設定（lmu-windows-auto-readout-pit-timing-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsAutoReadoutPitTimingDetailViewModel と、予想残り周回数・有効状態・両ソースの文言の Observe/Save UseCase。
 * UseCase が依存する設定・文言の Repository は :core:data の
 * desktopDataModule / androidDataModule で束ねられる。試聴用の named(Simulator.LmuWindows.id) の
 * PlayStartSoundForKeyUseCase・SpeakTextUseCase は :feature:lmu-windows-narrator で登録される。
 * 消費: TextToSpeechRepository・SoundVolumePreferencesRepository と既存のピットタイミング設定Repository。
 */
val lmuWindowsAutoReadoutPitTimingDetailModule =
    module {
        viewModel {
            LmuWindowsAutoReadoutPitTimingDetailViewModel(
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
