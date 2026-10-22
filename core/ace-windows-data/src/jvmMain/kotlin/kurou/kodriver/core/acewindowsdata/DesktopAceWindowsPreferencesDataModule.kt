package kurou.kodriver.core.acewindowsdata

import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsFlagPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop 版の AceWindows 設定系バインド（LongMethod 対策）。
 */
fun desktopAceWindowsPreferencesDataModule(directory: String): Module =
    module {
        single<AceWindowsMyBestLapPreferencesRepository> {
            createAceWindowsMyBestLapPreferencesRepository(directory = directory)
        }
        single<AceWindowsRemainingFuelPreferencesRepository> {
            createAceWindowsRemainingFuelPreferencesRepository(directory = directory)
        }
        single<AceWindowsRemainingFuelLapsPreferencesRepository> {
            createAceWindowsRemainingFuelLapsPreferencesRepository(directory = directory)
        }
        single<AceWindowsFlagPreferencesRepository> {
            createAceWindowsFlagPreferencesRepository(directory = directory)
        }
        single<AceWindowsFlagReadoutTextPreferencesRepository> {
            createAceWindowsFlagReadoutTextPreferencesRepository(directory = directory)
        }
        single<AceWindowsTyreTemperaturePreferencesRepository> {
            createAceWindowsTyreTemperaturePreferencesRepository(directory = directory)
        }
        single<AceWindowsVehicleApproachPreferencesRepository> {
            createAceWindowsVehicleApproachPreferencesRepository(directory = directory)
        }
    }
