package kurou.kodriver.data

import kurou.kodriver.data.preferences.LmuWindowsPitTimingPreferencesRepositories
import kurou.kodriver.data.preferences.LmuWindowsTyreTemperaturePreferencesRepositories
import kurou.kodriver.data.preferences.LmuWindowsVehicleApproachPreferencesRepositories
import kurou.kodriver.data.preferences.createLmuWindowsFlagPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop 版の LmuWindows 設定系バインド（LongMethod 対策）。
 */
internal fun desktopDataModuleLmuWindows(): Module =
    module {
        single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
            createLmuWindowsVehicleApproachThresholdsPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsFlagPreferencesRepository> {
            createLmuWindowsFlagPreferencesRepository(directory = kodriverDirectory)
        }
        single {
            createLmuWindowsVehicleApproachPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsVehicleApproachPreferencesRepository> {
            get<LmuWindowsVehicleApproachPreferencesRepositories>().preferences
        }
        single<LmuWindowsVehicleApproachReadoutTextPreferencesRepository> {
            get<LmuWindowsVehicleApproachPreferencesRepositories>().readoutText
        }
        single<LmuWindowsVehicleDamagePreferencesRepository> {
            createLmuWindowsVehicleDamagePreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsMyBestLapPreferencesRepository> {
            createLmuWindowsMyBestLapPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsFlagReadoutTextPreferencesRepository> {
            createLmuWindowsFlagReadoutTextPreferencesRepository(directory = kodriverDirectory)
        }
        single {
            createLmuWindowsTyreTemperaturePreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsTyreTemperaturePreferencesRepository> {
            get<LmuWindowsTyreTemperaturePreferencesRepositories>().preferences
        }
        single<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository> {
            get<LmuWindowsTyreTemperaturePreferencesRepositories>().readoutText
        }
        single<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsTyreWearPreferencesRepository> {
            createLmuWindowsTyreWearPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsVehicleClassBrakeWearPreferencesRepository> {
            createLmuWindowsVehicleClassBrakeWearPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsRemainingVirtualEnergyPreferencesRepository> {
            createLmuWindowsRemainingVirtualEnergyPreferencesRepository(directory = kodriverDirectory)
        }
        single {
            createLmuWindowsPitTimingPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsPitTimingPreferencesRepository> {
            get<LmuWindowsPitTimingPreferencesRepositories>().preferences
        }
        single<LmuWindowsPitTimingReadoutTextPreferencesRepository> {
            get<LmuWindowsPitTimingPreferencesRepositories>().readoutText
        }
    }
