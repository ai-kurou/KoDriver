package kurou.kodriver.feature.gt7ps5narrator

import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformSoundModule: Module = module {
    single<SoundPlayer> { JvmSoundPlayer() }
}
