package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * ドメインの UseCase を一元的に登録する共有モジュール。
 *
 * UseCase はステートレスな共有部品であり、以前は各 feature モジュールが必要な
 * UseCase をそれぞれ独自に登録していたため、同一型が複数モジュールで重複登録されたり
 * （Koin の後勝ち上書きに依存）、あるモジュールが別 feature モジュールの登録に暗黙依存
 * するなどの問題があった。
 *
 * 修飾子なしのドメイン UseCase はすべてここで 1 度だけ登録する。各 feature モジュールは
 * ViewModel・feature 固有の集約クラス・named 修飾が必要な登録（TTS 依存の
 * [kurou.kodriver.domain.usecase.PlaySpeechEventUseCase] 等）のみを持つ。
 */
val sharedUseCaseModule: Module = module {
    factoryOf(::CheckAppUpdateAvailableUseCase)
    factoryOf(::CheckGt7Ps5ConnectionUseCase)
    factoryOf(::CheckLmuWindowsConnectionUseCase)
    factoryOf(::DetermineGt7Ps5NarratorReadoutUseCase)
    factoryOf(::DetermineLmuWindowsNarratorReadoutUseCase)
    factoryOf(::FetchServerVersionUseCase)
    factoryOf(::LmuWindowsVehicleApproachPreferencesUseCases)
    factoryOf(::ObserveConsoleAddressUseCase)
    factoryOf(::ObserveExitConfirmationEnabledUseCase)
    factoryOf(::ObserveGt7Ps5MyBestLapVoiceTypeUseCase)
    factoryOf(::ObserveGt7Ps5RemainingFuelLapsUseCase)
    factoryOf(::ObserveGt7Ps5UdpPortUseCase)
    factoryOf(::ObserveGt7Ps5UseCase)
    factoryOf(::ObserveKeepScreenOnEnabledUseCase)
    factoryOf(::ObserveLmuWindowsFlagEnabledStatesUseCase)
    factoryOf(::ObserveLmuWindowsMyBestLapVoiceTypeUseCase)
    factoryOf(::ObserveLmuWindowsRaceFlagsUseCase)
    factoryOf(::ObserveLmuWindowsTyreCarcassTemperatureUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
    factoryOf(::ObserveLmuWindowsUseCase)
    factoryOf(::ObserveLmuWindowsVehicleApproachLateralThresholdUseCase)
    factoryOf(::ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase)
    factoryOf(::ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase)
    factoryOf(::ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase)
    factoryOf(::ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase)
    factoryOf(::ObserveLmuWindowsVehicleApproachUseCase)
    factoryOf(::ObserveLmuWindowsVehicleDamageEnabledStatesUseCase)
    factoryOf(::ObserveLmuWindowsVehicleDamageUseCase)
    factoryOf(::ObserveReadoutEnabledStatesUseCase)
    factoryOf(::ObserveReadoutOrderUseCase)
    factoryOf(::ObserveReadoutStartSoundTypeUseCase)
    factoryOf(::ObserveSelectedSimulatorUseCase)
    factoryOf(::ObserveServerIpUseCase)
    factoryOf(::ObserveSoundVolumeUseCase)
    factoryOf(::ObserveTelemetryLogDetailUseCase)
    factoryOf(::ObserveTelemetryLogsUseCase)
    factoryOf(::ObserveThemeModeUseCase)
    factoryOf(::ResetTelemetryLogDatabaseUseCase)
    factoryOf(::SaveConsoleAddressUseCase)
    factoryOf(::SaveExitConfirmationEnabledUseCase)
    factoryOf(::SaveGt7Ps5MyBestLapVoiceTypeUseCase)
    factoryOf(::SaveGt7Ps5RemainingFuelLapsUseCase)
    factoryOf(::SaveGt7Ps5UdpPortUseCase)
    factoryOf(::SaveKeepScreenOnEnabledUseCase)
    factoryOf(::SaveLmuWindowsFlagEnabledStateUseCase)
    factoryOf(::SaveLmuWindowsMyBestLapVoiceTypeUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureEnabledStateUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
    factoryOf(::SaveLmuWindowsVehicleApproachLateralThresholdUseCase)
    factoryOf(::SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase)
    factoryOf(::SaveLmuWindowsVehicleDamageEnabledStateUseCase)
    factoryOf(::SaveReadoutEnabledStateUseCase)
    factoryOf(::SaveReadoutOrderUseCase)
    factoryOf(::SaveReadoutStartSoundTypeUseCase)
    factoryOf(::SaveSelectedSimulatorUseCase)
    factoryOf(::SaveServerIpUseCase)
    factoryOf(::SaveSoundVolumeUseCase)
    factoryOf(::SaveTelemetryLogUseCase)
    factoryOf(::SaveThemeModeUseCase)
}
