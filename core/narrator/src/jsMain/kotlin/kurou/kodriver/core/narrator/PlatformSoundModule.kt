package kurou.kodriver.core.narrator

import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

actual fun platformSoundModule(qualifier: Qualifier): Module =
    module {
        single<SoundPlayer>(qualifier) { JsSoundPlayer() }
    }
