package kurou.kodriver.core.windowshotkeydata

import kurou.kodriver.core.windowshotkeydata.repository.HotkeyRepositoryImpl
import kurou.kodriver.domain.repository.HotkeyRepository
import org.koin.dsl.module

/** F9 固定の読み上げホットキー。非 Windows の空 Flow は Source が提供する。 */
val windowsHotkeyDataModule =
    module {
        single<GlobalHotkeySource> { WindowsGlobalHotkeySource.create() }
        single<HotkeyRepository> { HotkeyRepositoryImpl(source = get()) }
    }
