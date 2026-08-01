package kurou.kodriver.feature.gt7ps5narrator

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * WAV 音声再生に使う SoundPlayer のプラットフォーム実装をバインドする expect/actual モジュール。
 * narrator モジュールの includes(platformSoundModule) で取り込まれ、TextToSpeechEngine が get() で解決する。
 */
internal actual val platformSoundModule: Module =
    module {
    single<SoundPlayer> { JsSoundPlayer() }
}
