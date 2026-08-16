package kurou.kodriver.core.acewindowsdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsFlagRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsFuelRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsStatusRepositoryImpl
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsTyreCarcassTemperatureRepositoryImpl
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
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
        single<AceWindowsFlagRepository> {
            if (isWindows) AceWindowsFlagRepositoryImpl(source = get()) else NoOpAceWindowsFlagRepository()
        }
        single<AceWindowsStatusRepository> {
            if (isWindows) AceWindowsStatusRepositoryImpl(source = get()) else NoOpAceWindowsStatusRepository()
        }
        single<AceWindowsTyreCarcassTemperatureRepository> {
            if (isWindows) {
                AceWindowsTyreCarcassTemperatureRepositoryImpl(source = get())
            } else {
                NoOpAceWindowsTyreCarcassTemperatureRepository()
            }
        }
    }

private class NoOpAceWindowsFuelRepository : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false
}

private class NoOpAceWindowsFlagRepository : AceWindowsFlagRepository {
    override fun flagStream(): Flow<AceWindowsFlagData> = emptyFlow()
}

private class NoOpAceWindowsStatusRepository : AceWindowsStatusRepository {
    override fun statusStream(): Flow<AceWindowsStatusData> = emptyFlow()
}

private class NoOpAceWindowsTyreCarcassTemperatureRepository : AceWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData> = emptyFlow()
}
