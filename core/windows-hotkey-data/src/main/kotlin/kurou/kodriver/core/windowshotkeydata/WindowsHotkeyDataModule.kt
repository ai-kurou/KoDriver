package kurou.kodriver.core.windowshotkeydata

import kurou.kodriver.core.windowshotkeydata.repository.HotkeyRepositoryImpl
import kurou.kodriver.core.windowshotkeydata.repository.ManualReadoutHotkeyRepositoryImpl
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.HotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** LMU の現在値と全シミュレーターの手動セットの読み上げホットキー。登録は Repository 内で共有する。 */
val windowsHotkeyDataModule =
    module {
        single<GlobalHotkeySource> { WindowsGlobalHotkeySource.create() }
        single<ManualReadoutHotkeyRepository> {
            ManualReadoutHotkeyRepositoryImpl(
                source = get(),
                setRepositories = Simulator.entries.associate { it.id to get(named(it.id)) },
            )
        }
        single<HotkeyRepository> { HotkeyRepositoryImpl(source = get(), preferences = get()) }
    }
