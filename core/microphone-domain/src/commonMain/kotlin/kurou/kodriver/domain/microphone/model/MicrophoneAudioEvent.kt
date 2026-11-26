package kurou.kodriver.domain.microphone.model

/**
 * マイクから取得した音声のイベント。
 */
public sealed interface MicrophoneAudioEvent {
    /**
     * 取得した音声の断片。形式は [MICROPHONE_SAMPLE_RATE_HZ]・[MICROPHONE_CHANNEL_COUNT]・
     * [MICROPHONE_SAMPLE_SIZE_BITS] で固定の符号付きPCM（リトルエンディアン）。
     */
    public class Chunk(
        public val pcm: ByteArray,
    ) : MicrophoneAudioEvent {
        override fun equals(other: Any?): Boolean = other is Chunk && pcm.contentEquals(other.pcm)

        override fun hashCode(): Int = pcm.contentHashCode()

        override fun toString(): String = "Chunk(size=${pcm.size})"
    }

    /** 取得を続けられない。通知後にストリームは完了する。 */
    public data class Failed(
        val reason: MicrophoneFailureReason,
    ) : MicrophoneAudioEvent
}
