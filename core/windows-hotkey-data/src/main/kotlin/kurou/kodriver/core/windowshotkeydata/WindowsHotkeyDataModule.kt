package kurou.kodriver.core.windowshotkeydata

import kurou.kodriver.core.windowshotkeydata.repository.ManualReadoutHotkeyRepositoryImpl
import kurou.kodriver.core.windowshotkeydata.repository.WakeWordHotkeyRepositoryImpl
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** 手動セットの読み上げとウェイクワードのホットキー。登録は Repository 内で共有する。 */
public val windowsHotkeyDataModule: Module =
    module {
        single<GlobalHotkeySource> { WindowsGlobalHotkeySource.create() }
        single<ManualReadoutHotkeyRepository> {
            ManualReadoutHotkeyRepositoryImpl(
                source = get(),
                setRepositories = Simulator.entries.associate { it.id to get(named(it.id)) },
            )
        }
        single<WakeWordHotkeyRepository> {
            WakeWordHotkeyRepositoryImpl(source = get(), preferencesRepository = get())
        }
    }
