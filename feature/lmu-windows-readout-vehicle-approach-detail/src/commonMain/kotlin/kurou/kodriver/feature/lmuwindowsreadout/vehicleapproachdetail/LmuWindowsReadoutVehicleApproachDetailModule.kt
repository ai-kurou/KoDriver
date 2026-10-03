package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 車両接近アナウンス詳細設定（lmu-windows-readout-vehicle-approach-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutVehicleApproachDetailViewModel と、それが使うドメイン UseCase
 *   （閾値設定をまとめた LmuWindowsVehicleApproachThresholdsUseCases、開始読み上げ設定をまとめた
 *   LmuWindowsVehicleApproachPreferencesUseCases、ReadoutItemKeyベースのスイッチ状態を扱う
 *   ObserveLmuWindowsVehicleApproachEnabledStatesUseCase / SaveLmuWindowsVehicleApproachEnabledStateUseCase）。
 * 消費（get で解決）: LmuWindowsVehicleApproachThresholdsPreferencesRepository・
 *   LmuWindowsVehicleApproachPreferencesRepository・
 *   LmuWindowsVehicleApproachReadoutTextPreferencesRepository・SoundVolumePreferencesRepository（:core:data）、
 *   TextToSpeechRepository（:core:text-to-speech-data）、試聴用の named(Simulator.LmuWindows.id) の
 *   PlayStartSoundForKeyUseCase・SpeakTextUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutVehicleApproachDetailModule =
    module {
        // ViewModel（get(named(Simulator.LmuWindows.id)) は narrator モジュールの TextToSpeechEngine を解決）
        viewModel {
            LmuWindowsReadoutVehicleApproachDetailViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }

        factory {
            StartReadoutUseCases(
                get(named(Simulator.LmuWindows.id)),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
                get(),
                get(),
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { LmuWindowsVehicleApproachThresholdsUseCases(get()) }
        factory { LmuWindowsVehicleApproachPreferencesUseCases(get(), get()) }
        factory { ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(get()) }
        factory { CheckTextToSpeechAvailableUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsVehicleApproachEnabledStateUseCase(get()) }
    }
