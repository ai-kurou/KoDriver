package kurou.kodriver.feature.lmuwindowsnarrator

import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformSoundModule: Module = module {
    single<SoundPlayer> { WasmJsSoundPlayer() }
}
