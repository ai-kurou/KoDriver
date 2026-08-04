package kurou.kodriver.core.narrator

import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier

/**
 * WAV 音声再生に使う [SoundPlayer] のプラットフォーム実装を、[qualifier] 付きでバインドする
 * expect/actual モジュール。呼び出し側（各 narrator feature）は自身を識別する named 修飾子を渡し、
 * 同一 Koin コンテナ内で複数の narrator feature が [SoundPlayer] を同時に登録しても衝突しないようにする。
 */
expect fun platformSoundModule(qualifier: Qualifier): Module
