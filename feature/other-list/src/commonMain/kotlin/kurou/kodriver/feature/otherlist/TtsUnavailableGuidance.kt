package kurou.kodriver.feature.otherlist

/**
 * 起動時のダイアログで案内するTTS利用不可の種類。
 * `:app:shared` が `:core:domain` へ依存せずに表示を切り替えられるよう、feature側で公開する。
 */
enum class TtsUnavailableGuidance {
    EngineMissing,
    LanguageDataMissing,
    WindowsSpeechUnavailable,
}
