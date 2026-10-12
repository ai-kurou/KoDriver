package kurou.kodriver.core.windowshotkeydata

import kurou.kodriver.core.windowshotkeydata.repository.HotkeyRepositoryImpl
import kurou.kodriver.domain.repository.HotkeyRepository
import org.koin.dsl.module

/** LMU の現在値読み上げホットキー。登録は Repository 内で共有する。 */
val windowsHotkeyDataModule =
    module {
        single<GlobalHotkeySource> { WindowsGlobalHotkeySource.create() }
        single<HotkeyRepository> { HotkeyRepositoryImpl(source = get(), preferences = get()) }
    }
