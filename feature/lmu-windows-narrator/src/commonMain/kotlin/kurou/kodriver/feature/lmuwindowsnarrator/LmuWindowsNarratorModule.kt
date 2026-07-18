package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * LMU アナウンス制御（lmu-windows-narrator feature）の Koin モジュール。
 *
 * 提供: LmuWindowsNarratorViewModel、この feature 内で定義した UseCase 集約 data class
 *   （NarratorUseCases / FlagUseCases / VehicleApproachUseCases / VehicleDamageUseCases /
 *   ReadoutListUseCases / TyreTemperatureUseCases）、それらが束ねる各ドメイン UseCase、
 *   および named("lmu_windows") の音声再生系（PlaySpeechEventUseCase・TextToSpeechEngine）。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:lmu-windows-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）。
 * 音声系は GT7 と区別するため named("lmu_windows") で登録している。
 */
val lmuWindowsNarratorModule: Module = module {
    // ViewModel（get(named "lmu_windows") は下記の TextToSpeechEngine を解決）
    viewModel { LmuWindowsNarratorViewModel(get(), get(), get(), get(), get(), get(named("lmu_windows")), get()) }

    // この feature 固有の UseCase 集約 data class（本モジュールで定義）
    factory { NarratorUseCases(get(), get(), get(), get(), get(), get()) }
    factory { FlagUseCases(get(), get()) }
    factory { VehicleApproachUseCases(get(), get(), get(), get(), get(), get(), get()) }
    factory { VehicleDamageUseCases(get(), get()) }
    factory { ReadoutListUseCases(get(), get(), get()) }
    factory { TyreTemperatureUseCases(get(), get(), get(), get()) }

    // ドメイン UseCase（:core:domain。get() は :core:lmu-windows-data / :core:data の Repository を解決）
    factory { DetermineLmuWindowsNarratorReadoutUseCase() }
    factory { SaveTelemetryLogUseCase(get()) }
    factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
    factory { ObserveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
    factory { ObserveLmuWindowsRedFlagVoiceTypeUseCase(get()) }
    factory { ObserveLmuWindowsUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachUseCase(get()) }
    factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase(get()) }
    factory { ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(get()) }
    factory { ObserveLmuWindowsVehicleDamageUseCase(get()) }
    factory { ObserveLmuWindowsTyreCarcassTemperatureUseCase(get()) }
    factory { ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(get()) }
    factory { ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(get()) }
    factory { ObserveLmuWindowsVirtualEnergyUseCase(get()) }
    factory { ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase(get()) }

    // 音声再生（named "lmu_windows" で GT7 と分離。SoundPlayer は platformSoundModule が提供）
    factory(named("lmu_windows")) { PlaySpeechEventUseCase(get(named("lmu_windows"))) }
    includes(platformSoundModule)
    single<TextToSpeechEngine>(named("lmu_windows")) {
        LmuWindowsWavNarratorEngine(
            soundPlayer = get(),
            volumeFlow = ObserveSoundVolumeUseCase(get())(),
            startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
        )
    }
}

internal expect val platformSoundModule: Module
