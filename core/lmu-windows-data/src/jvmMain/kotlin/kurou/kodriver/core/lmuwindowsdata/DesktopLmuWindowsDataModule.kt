package kurou.kodriver.core.lmuwindowsdata

import kurou.kodriver.core.lmuwindowsdata.preferences.LmuWindowsPitTimingPreferencesRepositories
import kurou.kodriver.core.lmuwindowsdata.preferences.LmuWindowsTyreTemperaturePreferencesRepositories
import kurou.kodriver.core.lmuwindowsdata.preferences.LmuWindowsVehicleApproachPreferencesRepositories
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsFlagPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.core.lmuwindowsdata.preferences.createLmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop 版の LmuWindows 設定系バインド（LongMethod 対策）。
 */
fun desktopLmuWindowsPreferencesDataModule(directory: String): Module =
    module {
        single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
            createLmuWindowsVehicleApproachThresholdsPreferencesRepository(directory = directory)
        }
        single<LmuWindowsFlagPreferencesRepository> {
            createLmuWindowsFlagPreferencesRepository(directory = directory)
        }
        single {
            createLmuWindowsVehicleApproachPreferencesRepository(directory = directory)
        }
        single<LmuWindowsVehicleApproachPreferencesRepository> {
            get<LmuWindowsVehicleApproachPreferencesRepositories>().preferences
        }
        single<LmuWindowsVehicleApproachReadoutTextPreferencesRepository> {
            get<LmuWindowsVehicleApproachPreferencesRepositories>().readoutText
        }
        single<LmuWindowsVehicleDamagePreferencesRepository> {
            createLmuWindowsVehicleDamagePreferencesRepository(directory = directory)
        }
        single<LmuWindowsMyBestLapPreferencesRepository> {
            createLmuWindowsMyBestLapPreferencesRepository(directory = directory)
        }
        single<LmuWindowsFlagReadoutTextPreferencesRepository> {
            createLmuWindowsFlagReadoutTextPreferencesRepository(directory = directory)
        }
        single {
            createLmuWindowsTyreTemperaturePreferencesRepository(directory = directory)
        }
        single<LmuWindowsTyreTemperaturePreferencesRepository> {
            get<LmuWindowsTyreTemperaturePreferencesRepositories>().preferences
        }
        single<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository> {
            get<LmuWindowsTyreTemperaturePreferencesRepositories>().readoutText
        }
        single<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(directory = directory)
        }
        single<LmuWindowsTyreWearPreferencesRepository> {
            createLmuWindowsTyreWearPreferencesRepository(directory = directory)
        }
        single<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository(directory = directory)
        }
        single<LmuWindowsVehicleClassBrakeWearPreferencesRepository> {
            createLmuWindowsVehicleClassBrakeWearPreferencesRepository(directory = directory)
        }
        single<LmuWindowsRemainingVirtualEnergyPreferencesRepository> {
            createLmuWindowsRemainingVirtualEnergyPreferencesRepository(directory = directory)
        }
        single<LmuWindowsRemainingFuelPreferencesRepository> {
            createLmuWindowsRemainingFuelPreferencesRepository(
                directory,
            )
        }
        single {
            createLmuWindowsPitTimingPreferencesRepository(directory = directory)
        }
        single<LmuWindowsPitTimingPreferencesRepository> {
            get<LmuWindowsPitTimingPreferencesRepositories>().preferences
        }
        single<LmuWindowsPitTimingReadoutTextPreferencesRepository> {
            get<LmuWindowsPitTimingPreferencesRepositories>().readoutText
        }
    }
