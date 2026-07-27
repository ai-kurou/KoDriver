package kurou.kodriver.core.acewindowsdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.repository.AceWindowsFuelRepositoryImpl
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import org.koin.dsl.module

private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

/**
 * ACE (Assetto Corsa EVO) 共有メモリの Repository バインドを行う Koin モジュール
 * (:core:ace-windows-data。JVM 専用)。
 *
 * 共有メモリ読み取りは Windows 専用のため、非 Windows では空 Flow を返す
 * No-Op 実装（下部の private class）にフォールバックする。
 */
val aceWindowsDataModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single { AceWindowsGraphicsSharedMemorySource(scope = get()) }

    single<AceWindowsFuelRepository> {
        if (isWindows) AceWindowsFuelRepositoryImpl(source = get()) else NoOpAceWindowsFuelRepository()
    }
}

private class NoOpAceWindowsFuelRepository : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = emptyFlow()
}
