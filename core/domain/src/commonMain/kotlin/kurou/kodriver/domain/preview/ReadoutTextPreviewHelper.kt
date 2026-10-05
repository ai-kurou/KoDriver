package kurou.kodriver.domain.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * 解決済みの自由文言を、項目の開始音の後に試聴する。
 * 利用可否は所有者の [scope] で一度取得し、購読の有無にかかわらず保持する。
 * 再生は呼び出し元のコルーチンで実行し、キャンセルと例外をそのまま伝播する。
 */
class ReadoutTextPreviewHelper(
    scope: CoroutineScope,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
    private val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    private val speakText: SpeakTextUseCase,
) {
    val textToSpeechAvailable =
        flow { emit(checkTextToSpeechAvailable()) }
            .stateIn(scope, SharingStarted.Eagerly, false)

    /** 空白文言・TTS利用不可・音量0以下では開始音も本文も再生しない。 */
    suspend fun preview(
        text: String,
        key: ReadoutItemKey,
    ) {
        if (text.isBlank() || !textToSpeechAvailable.value) return
        val volume = observeSoundVolume().first()
        if (volume <= 0) return
        playStartSoundForKey(key)
        speakText(text, volume = volume)
    }
}
