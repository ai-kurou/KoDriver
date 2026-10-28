package kurou.kodriver.core.acewindowsdata

import android.content.Context
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsFlagPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.core.acewindowsdata.preferences.createAceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsBestLapTimeRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsFlagRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsFuelRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsGapRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsRemainingFuelLapsRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsStatusRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.core.acewindowsdata.websocket.WebSocketAceWindowsVehicleApproachRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsGapRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Android 版の AceWindows WebSocket・設定系バインド（LongMethod 対策）。
 */
fun androidAceWindowsDataModule(context: Context): Module =
    module {
        single {
            AceWindowsAutoReadoutSettingsResetter(
                flagPreferencesRepository = get(),
                flagReadoutTextPreferencesRepository = get(),
                myBestLapPreferencesRepository = get(),
                remainingFuelLapsPreferencesRepository = get(),
                remainingFuelPreferencesRepository = get(),
                tyreTemperaturePreferencesRepository = get(),
                vehicleApproachPreferencesRepository = get(),
            )
        } bind AutoReadoutSettingsResetter::class
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
        single<AceWindowsGapRepository> {
            WebSocketAceWindowsGapRepository(serverIpRepository = get(), client = get())
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
