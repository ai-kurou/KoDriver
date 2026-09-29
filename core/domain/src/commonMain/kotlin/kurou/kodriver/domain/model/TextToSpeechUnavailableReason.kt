package kurou.kodriver.domain.model

/**
 * OS標準TTSが利用できない理由。
 *
 * Androidではエンジンと日本語データの不足を判別し、Windowsでは利用できない場合に
 * Windowsの音声設定を案内するために使う。
 */
enum class TextToSpeechUnavailableReason {
    /** TTSエンジン自体が端末にインストールされていない。 */
    EngineMissing,

    /** TTSエンジンは利用できるが、読み上げに使う言語（日本語）の音声データがインストールされていない。 */
    LanguageDataMissing,

    /** Windowsの音声合成、または日本語音声を利用できない。 */
    WindowsSpeechUnavailable,
}
