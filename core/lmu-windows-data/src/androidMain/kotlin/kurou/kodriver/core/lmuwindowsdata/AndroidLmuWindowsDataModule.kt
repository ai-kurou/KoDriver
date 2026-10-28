package kurou.kodriver.core.lmuwindowsdata

import android.content.Context
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
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsBrakeTemperatureRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsBrakeWearRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsCarGapRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsClassCarGapRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsFlagRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsPitStatusRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsTyreDetachedRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsTyreWearRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsVehicleApproachRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsVehicleClassRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsVehicleDamageRepository
import kurou.kodriver.core.lmuwindowsdata.websocket.WebSocketLmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsClassCarGapRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Android 版の LMU WebSocket バインド。設定系は別モジュールに分離する（LongMethod 対策）。
 */
fun androidLmuWindowsDataModule(context: Context): Module =
    module {
        single {
            LmuWindowsAutoReadoutSettingsResetter(
                flagPreferencesRepository = get(),
                flagReadoutTextPreferencesRepository = get(),
                myBestLapPreferencesRepository = get(),
                pitTimingPreferencesRepository = get(),
                remainingFuelPreferencesRepository = get(),
                remainingVirtualEnergyPreferencesRepository = get(),
                tyreTemperaturePreferencesRepository = get(),
                tyreWearPreferencesRepository = get(),
                vehicleApproachPreferencesRepository = get(),
                vehicleApproachThresholdsPreferencesRepository = get(),
                vehicleClassBrakeTemperaturePreferencesRepository = get(),
                vehicleClassBrakeWearPreferencesRepository = get(),
                vehicleClassTyreTemperaturePreferencesRepository = get(),
                vehicleDamagePreferencesRepository = get(),
            )
        } bind AutoReadoutSettingsResetter::class
        includes(androidLmuWindowsPreferencesDataModule(context))
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
        single<LmuWindowsCarGapRepository> {
            WebSocketLmuWindowsCarGapRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsClassCarGapRepository> {
            WebSocketLmuWindowsClassCarGapRepository(serverIpRepository = get(), client = get())
        }
    }

/**
 * Android 版の LMU 設定系 DataStore バインド（LongMethod 対策）。
 */
private fun androidLmuWindowsPreferencesDataModule(context: Context): Module =
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
        single<LmuWindowsRemainingFuelPreferencesRepository> {
            createLmuWindowsRemainingFuelPreferencesRepository(context.filesDir.absolutePath)
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
