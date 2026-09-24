package kurou.kodriver.core.acewindowsdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsBestLapTimeRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsBrakeWearRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsFlagRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsFuelRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsRemainingFuelLapsRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsStatusRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsTyreCarcassTemperatureRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsVehicleApproachRepositoryImpl
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsBrakeWearData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.repository.AceWindowsBrakeWearRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import org.koin.dsl.module

private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

/**
 * ACE (Assetto Corsa EVO) 共有メモリの Repository バインドを行う Koin モジュール
 * (:core:ace-windows-data。JVM 専用)。
 *
 * 共有メモリ読み取りは Windows 専用のため、非 Windows では空 Flow を返す
 * No-Op 実装（下部の private class）にフォールバックする。
 */
val aceWindowsDataModule =
    module {
        single { CoroutineScope(SupervisorJob()) }
        single { AceWindowsGraphicsSharedMemorySource(scope = get()) }

        single<AceWindowsFuelRepository> {
            if (isWindows) AceWindowsFuelRepositoryImpl(source = get()) else NoOpAceWindowsFuelRepository()
        }
        single<AceWindowsRemainingFuelLapsRepository> {
            if (isWindows) {
                AceWindowsRemainingFuelLapsRepositoryImpl(source = get())
            } else {
                NoOpAceWindowsRemainingFuelLapsRepository()
            }
        }
        single<AceWindowsFlagRepository> {
            if (isWindows) AceWindowsFlagRepositoryImpl(source = get()) else NoOpAceWindowsFlagRepository()
        }
        single<AceWindowsStatusRepository> {
            if (isWindows) AceWindowsStatusRepositoryImpl(source = get()) else NoOpAceWindowsStatusRepository()
        }
        single<AceWindowsBestLapTimeRepository> {
            if (isWindows) {
                AceWindowsBestLapTimeRepositoryImpl(source = get())
            } else {
                NoOpAceWindowsBestLapTimeRepository()
            }
        }
        single<AceWindowsTyreCarcassTemperatureRepository> {
            if (isWindows) {
                AceWindowsTyreCarcassTemperatureRepositoryImpl(source = get())
            } else {
                NoOpAceWindowsTyreCarcassTemperatureRepository()
            }
        }
        single<AceWindowsVehicleApproachRepository> {
            if (isWindows) {
                AceWindowsVehicleApproachRepositoryImpl(source = get())
            } else {
                NoOpAceWindowsVehicleApproachRepository()
            }
        }
        single<AceWindowsBrakeWearRepository> {
            if (isWindows) AceWindowsBrakeWearRepositoryImpl(source = get()) else NoOpAceWindowsBrakeWearRepository()
        }
    }

private class NoOpAceWindowsFuelRepository : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false
}

private class NoOpAceWindowsRemainingFuelLapsRepository : AceWindowsRemainingFuelLapsRepository {
    override fun remainingFuelLapsStream(): Flow<AceWindowsRemainingFuelLapsData> = emptyFlow()
}

private class NoOpAceWindowsFlagRepository : AceWindowsFlagRepository {
    override fun flagStream(): Flow<AceWindowsFlagData> = emptyFlow()
}

private class NoOpAceWindowsStatusRepository : AceWindowsStatusRepository {
    override fun statusStream(): Flow<AceWindowsStatusData> = emptyFlow()
}

private class NoOpAceWindowsBestLapTimeRepository : AceWindowsBestLapTimeRepository {
    override fun bestLapTimeStream(): Flow<AceWindowsBestLapTimeData> = emptyFlow()
}

private class NoOpAceWindowsTyreCarcassTemperatureRepository : AceWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private class NoOpAceWindowsVehicleApproachRepository : AceWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<AceWindowsVehicleApproachData> = emptyFlow()
}

private class NoOpAceWindowsBrakeWearRepository : AceWindowsBrakeWearRepository {
    override fun brakeWearStream(): Flow<AceWindowsBrakeWearData> = emptyFlow()
}
