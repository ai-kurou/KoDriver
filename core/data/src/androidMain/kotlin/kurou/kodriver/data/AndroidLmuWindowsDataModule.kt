package kurou.kodriver.data

import android.content.Context
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
import kurou.kodriver.data.websocket.WebSocketLmuWindowsBrakeTemperatureRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsBrakeWearRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsFlagRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsPitStatusRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsTyreDetachedRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsTyreWearRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVehicleApproachRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVehicleClassRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVehicleDamageRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 版の LMU WebSocket バインド。設定系は別モジュールに分離する（LongMethod 対策）。
 */
internal fun androidDataModuleLmuWindows(context: Context): Module =
    module {
        includes(androidDataModuleLmuWindowsPreferences(context))
        single<LmuWindowsRepository> { WebSocketLmuWindowsRepository(serverIpRepository = get(), client = get()) }
        single<LmuWindowsFlagRepository> {
            WebSocketLmuWindowsFlagRepository(
                serverIpRepository = get(),
                client = get(),
            )
        }
        single<LmuWindowsVehicleApproachRepository> {
            WebSocketLmuWindowsVehicleApproachRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsVehicleDamageRepository> {
            WebSocketLmuWindowsVehicleDamageRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsTyreCarcassTemperatureRepository> {
            WebSocketLmuWindowsTyreCarcassTemperatureRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsBrakeWearRepository> {
            WebSocketLmuWindowsBrakeWearRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsBrakeTemperatureRepository> {
            WebSocketLmuWindowsBrakeTemperatureRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsVehicleClassRepository> {
            WebSocketLmuWindowsVehicleClassRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsTyreWearRepository> {
            WebSocketLmuWindowsTyreWearRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsVirtualEnergyRepository> {
            WebSocketLmuWindowsVirtualEnergyRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsPitStatusRepository> {
            WebSocketLmuWindowsPitStatusRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsTyreDetachedRepository> {
            WebSocketLmuWindowsTyreDetachedRepository(serverIpRepository = get(), client = get())
        }
    }

/**
 * Android 版の LMU 設定系 DataStore バインド（LongMethod 対策）。
 */
private fun androidDataModuleLmuWindowsPreferences(context: Context): Module =
    module {
        single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
            createLmuWindowsVehicleApproachThresholdsPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsFlagPreferencesRepository> {
            createLmuWindowsFlagPreferencesRepository(context.filesDir.absolutePath)
        }
        single {
            createLmuWindowsVehicleApproachPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsVehicleApproachPreferencesRepository> {
            get<LmuWindowsVehicleApproachPreferencesRepositories>().preferences
        }
        single<LmuWindowsVehicleApproachReadoutTextPreferencesRepository> {
            get<LmuWindowsVehicleApproachPreferencesRepositories>().readoutText
        }
        single<LmuWindowsVehicleDamagePreferencesRepository> {
            createLmuWindowsVehicleDamagePreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsMyBestLapPreferencesRepository> {
            createLmuWindowsMyBestLapPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsFlagReadoutTextPreferencesRepository> {
            createLmuWindowsFlagReadoutTextPreferencesRepository(context.filesDir.absolutePath)
        }
        single {
            createLmuWindowsTyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsTyreTemperaturePreferencesRepository> {
            get<LmuWindowsTyreTemperaturePreferencesRepositories>().preferences
        }
        single<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository> {
            get<LmuWindowsTyreTemperaturePreferencesRepositories>().readoutText
        }
        single<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsTyreWearPreferencesRepository> {
            createLmuWindowsTyreWearPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsVehicleClassBrakeWearPreferencesRepository> {
            createLmuWindowsVehicleClassBrakeWearPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsRemainingVirtualEnergyPreferencesRepository> {
            createLmuWindowsRemainingVirtualEnergyPreferencesRepository(context.filesDir.absolutePath)
        }
        single {
            createLmuWindowsPitTimingPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsPitTimingPreferencesRepository> {
            get<LmuWindowsPitTimingPreferencesRepositories>().preferences
        }
        single<LmuWindowsPitTimingReadoutTextPreferencesRepository> {
            get<LmuWindowsPitTimingPreferencesRepositories>().readoutText
        }
    }
