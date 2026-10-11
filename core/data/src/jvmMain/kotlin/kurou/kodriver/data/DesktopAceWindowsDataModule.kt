package kurou.kodriver.data

import kurou.kodriver.data.preferences.createAceWindowsFlagPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop 版の AceWindows 設定系バインド（LongMethod 対策）。
 */
internal fun desktopDataModuleAceWindows(): Module =
    module {
        single<AceWindowsMyBestLapPreferencesRepository> {
            createAceWindowsMyBestLapPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsRemainingFuelPreferencesRepository> {
            createAceWindowsRemainingFuelPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsRemainingFuelLapsPreferencesRepository> {
            createAceWindowsRemainingFuelLapsPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsFlagPreferencesRepository> {
            createAceWindowsFlagPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsFlagReadoutTextPreferencesRepository> {
            createAceWindowsFlagReadoutTextPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsTyreTemperaturePreferencesRepository> {
            createAceWindowsTyreTemperaturePreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsVehicleApproachPreferencesRepository> {
            createAceWindowsVehicleApproachPreferencesRepository(directory = kodriverDirectory)
        }
    }
