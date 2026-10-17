package kurou.kodriver.data

import android.content.Context
import kurou.kodriver.data.preferences.createAceWindowsFlagPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsBestLapTimeRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsFlagRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsFuelRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsRemainingFuelLapsRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsStatusRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsVehicleApproachRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 版の AceWindows WebSocket・設定系バインド（LongMethod 対策）。
 */
internal fun androidDataModuleAceWindows(context: Context): Module =
    module {
        single<AceWindowsMyBestLapPreferencesRepository> {
            createAceWindowsMyBestLapPreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsFuelRepository> {
            WebSocketAceWindowsFuelRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsFlagRepository> {
            WebSocketAceWindowsFlagRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsStatusRepository> {
            WebSocketAceWindowsStatusRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsTyreCarcassTemperatureRepository> {
            WebSocketAceWindowsTyreCarcassTemperatureRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsVehicleApproachRepository> {
            WebSocketAceWindowsVehicleApproachRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsBestLapTimeRepository> {
            WebSocketAceWindowsBestLapTimeRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsRemainingFuelLapsRepository> {
            WebSocketAceWindowsRemainingFuelLapsRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsRemainingFuelPreferencesRepository> {
            createAceWindowsRemainingFuelPreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsRemainingFuelLapsPreferencesRepository> {
            createAceWindowsRemainingFuelLapsPreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsFlagPreferencesRepository> {
            createAceWindowsFlagPreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsFlagReadoutTextPreferencesRepository> {
            createAceWindowsFlagReadoutTextPreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsTyreTemperaturePreferencesRepository> {
            createAceWindowsTyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsVehicleApproachPreferencesRepository> {
            createAceWindowsVehicleApproachPreferencesRepository(context.filesDir.absolutePath)
        }
    }
