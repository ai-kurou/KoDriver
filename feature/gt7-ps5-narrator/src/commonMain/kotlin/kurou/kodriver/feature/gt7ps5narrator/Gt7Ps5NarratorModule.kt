package kurou.kodriver.feature.gt7ps5narrator

import org.koin.core.module.Module
import org.koin.dsl.module

val gt7Ps5NarratorModule: Module = module {
    includes(platformSoundModule)
}

internal expect val platformSoundModule: Module
