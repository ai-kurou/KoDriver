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

    /**
     * Windowsの音声合成、または日本語音声を利用できない。
     *
     * 判定と読み上げはPowerShellの`System.Speech`（SAPI5）で行うため、Windows 11の設定で追加できる
     * Natural音声など、SAPI5に公開されない音声は判定対象に含まれない。SAPI5で使える日本語音声
     * （日本語の言語パックに含まれる音声など）が導入されるまで案内が表示され続ける。
     */
    WindowsSpeechUnavailable,
}
