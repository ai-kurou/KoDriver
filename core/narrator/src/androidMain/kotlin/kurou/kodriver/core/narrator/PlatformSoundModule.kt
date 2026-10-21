package kurou.kodriver.core.narrator

import android.content.Context
import android.media.AudioAttributes
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual fun platformSoundModule(qualifier: Qualifier): Module =
    module {
        single<SoundPlayer>(qualifier) {
            // :core:text-to-speech-data は同じ qualifier 名で保存済み用途を登録する。
            val usageProvider = getOrNull<suspend () -> Int>(named("android_sound_audio_usage"))
            AndroidSoundPlayer(get<Context>(), usageProvider ?: { AudioAttributes.USAGE_MEDIA })
        }
    }
