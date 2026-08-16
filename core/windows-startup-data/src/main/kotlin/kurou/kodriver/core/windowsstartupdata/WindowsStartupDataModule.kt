package kurou.kodriver.core.windowsstartupdata

import kurou.kodriver.core.windowsstartupdata.repository.WindowsStartupEnabledRepository
import kurou.kodriver.domain.repository.StartupEnabledRepository
import org.koin.dsl.module

private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

/**
 * OS起動時のKoDriver自動起動設定のRepositoryバインドを行うKoinモジュール（:core:windows-startup-data。JVM専用）。
 *
 * レジストリへのアクセスはWindows専用のため、非Windowsでは何もしないNo-Op実装（下部のprivate class）に
 * フォールバックする。
 */
val windowsStartupDataModule =
    module {
        single<StartupEnabledRepository> {
            if (isWindows) WindowsStartupEnabledRepository() else NoOpStartupEnabledRepository()
        }
    }

private class NoOpStartupEnabledRepository : StartupEnabledRepository {
    override suspend fun isEnabled(): Boolean = false

    override suspend fun setEnabled(enabled: Boolean) = Unit
}
