package kurou.kodriver.domain.model

/**
 * OS標準TTSが利用できない理由。
 *
 * Androidでのみ判別し、ユーザーに解決手段（アプリストアへの誘導・OS設定画面への誘導）を
 * 案内するために使う。Windows（デスクトップ）ではOS自体の判定のみで理由の切り分けは行わないため、
 * 常に `null`（判定対象外）を返す。
 */
enum class TextToSpeechUnavailableReason {
    /** TTSエンジン自体が端末にインストールされていない。 */
    EngineMissing,

    /** TTSエンジンは利用できるが、読み上げに使う言語（日本語）の音声データがインストールされていない。 */
    LanguageDataMissing,
}
