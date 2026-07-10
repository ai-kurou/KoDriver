package kurou.kodriver.feature.desktopsplash

/**
 * スプラッシュ画面の表示状態。
 *
 * @property step 現在実行中の初期化フェーズ。
 */
data class DesktopSplashUiState(
    val step: DesktopSplashStep = DesktopSplashStep.INITIALIZING_MODULES,
) {
    /** すべての初期化が完了しているか。 */
    val isReady: Boolean get() = step == DesktopSplashStep.READY

    /** 0.0〜1.0 の進捗率。プログレスインジケーター表示に使う。 */
    val progress: Float
        get() = (step.ordinal + 1).toFloat() / DesktopSplashStep.entries.size
}
