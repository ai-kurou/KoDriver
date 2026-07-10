package kurou.kodriver.feature.desktopsplash

/**
 * スプラッシュ画面の進捗を更新しながら、アプリの初期化を順に実行する。
 *
 * 各フェーズの開始時に [DesktopSplashProgress] を更新し、対応する処理を実行する。
 * すべて完了すると [DesktopSplashStep.READY] へ遷移する。
 *
 * ディスパッチャの切り替え（ブロッキング処理を UI スレッド外で実行する等）は、
 * この関数ではなく [initializeModules] / [startServer] の呼び出し側の責務とする。
 *
 * @param initializeModules Koin モジュール構築など、依存グラフの初期化処理。
 * @param startServer Ktor サーバーの起動処理。
 */
suspend fun DesktopSplashProgress.runInitialization(
    initializeModules: suspend () -> Unit,
    startServer: suspend () -> Unit,
) {
    update(DesktopSplashStep.INITIALIZING_MODULES)
    initializeModules()

    update(DesktopSplashStep.STARTING_SERVER)
    startServer()

    update(DesktopSplashStep.READY)
}
