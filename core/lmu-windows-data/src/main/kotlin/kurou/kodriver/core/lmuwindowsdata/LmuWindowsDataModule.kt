package kurou.kodriver.core.lmuwindowsdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsFlagRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsTyreCarcassTemperatureRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsTyreWearRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsVehicleApproachRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsVehicleClassRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsVehicleDamageRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsVirtualEnergyRepositoryImpl
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import org.koin.dsl.module

private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

/**
 * LMU 共有メモリの Repository バインドを行う Koin モジュール（:core:lmu-windows-data。JVM 専用）。
 *
 * デスクトップ版 app エントリーポイントで束ねられ、lmu-windows-connection / lmu-windows-narrator の
 * UseCase が get() で解決する各 LmuWindows*Repository を提供する。共有メモリ読み取りは Windows 専用のため、
 * 非 Windows では空 Flow を返す No-Op 実装（下部の private class 群）にフォールバックする。
 *
 * LmuWindowsVehicleClassRepository は Scoring セグメントの mVehicleClass（人間可読なクラス名文字列）を
 * プレイヤー車両分だけ読み取る。
 */
val lmuWindowsDataModule =
    module {
        // 共有メモリのポーリングを回すスコープとデータソース
        single { CoroutineScope(SupervisorJob()) }
        single { LmuWindowsSharedMemorySource(scope = get()) }

        // 各 Repository（Windows は共有メモリ実装、非 Windows は No-Op。get() でスコープ/ソース/閾値設定を解決）
        single<LmuWindowsRepository> {
            if (isWindows) LmuWindowsRepositoryImpl(source = get()) else NoOpLmuWindowsRepository()
        }
        single<LmuWindowsVehicleApproachRepository> {
            if (isWindows) {
                LmuWindowsVehicleApproachRepositoryImpl(thresholdsRepository = get(), source = get())
            } else {
                NoOpVehicleApproachRepository()
            }
        }
        single<LmuWindowsFlagRepository> {
            if (isWindows) LmuWindowsFlagRepositoryImpl(source = get()) else NoOpFlagRepository()
        }
        single<LmuWindowsVehicleDamageRepository> {
            if (isWindows) LmuWindowsVehicleDamageRepositoryImpl(source = get()) else NoOpVehicleDamageRepository()
        }
        single<LmuWindowsTyreCarcassTemperatureRepository> {
            if (isWindows) {
                LmuWindowsTyreCarcassTemperatureRepositoryImpl(source = get())
            } else {
                NoOpTyreCarcassTemperatureRepository()
            }
        }
        single<LmuWindowsTyreWearRepository> {
            if (isWindows) LmuWindowsTyreWearRepositoryImpl(source = get()) else NoOpTyreWearRepository()
        }
        single<LmuWindowsVehicleClassRepository> {
            if (isWindows) LmuWindowsVehicleClassRepositoryImpl(source = get()) else NoOpVehicleClassRepository()
        }
        single<LmuWindowsVirtualEnergyRepository> {
            if (isWindows) LmuWindowsVirtualEnergyRepositoryImpl(source = get()) else NoOpVirtualEnergyRepository()
        }
    }

private class NoOpLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false

    override suspend fun disconnect() = Unit
}

private class NoOpVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
}

private class NoOpFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}

private class NoOpVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private class NoOpTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private class NoOpTyreWearRepository : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = emptyFlow()
}

private class NoOpVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = emptyFlow()
}

private class NoOpVehicleClassRepository : LmuWindowsVehicleClassRepository {
    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> = emptyFlow()
}
