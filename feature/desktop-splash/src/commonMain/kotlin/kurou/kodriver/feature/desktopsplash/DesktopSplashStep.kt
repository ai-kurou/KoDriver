package kurou.kodriver.feature.desktopsplash

/**
 * デスクトップアプリ起動中に進行する初期化フェーズ。
 *
 * 各フェーズは main.kt 側の起動オーケストレーションから駆動され、
 * スプラッシュ画面がどの処理を実行中かを表示するために使う。
 * DataStore などに永続化される値ではないため、順序（[ordinal]）を
 * 進捗率の算出に利用してよい。
 */
enum class DesktopSplashStep {
    /** Koin モジュール構築中。 */
    INITIALIZING_MODULES,

    /** Ktor サーバー起動中。 */
    STARTING_SERVER,

    /** すべての初期化が完了し、メイン画面へ切り替え可能な状態。 */
    READY,
}

/**
 * スプラッシュ画面に表示する日本語のフェーズ名。
 *
 * 内部の識別子（[DesktopSplashStep] の名前）と表示名を分離するため、
 * 表示名はこの拡張プロパティ側に持たせる。
 */
val DesktopSplashStep.displayName: String
    get() = when (this) {
        DesktopSplashStep.INITIALIZING_MODULES -> "モジュールを初期化しています…"
        DesktopSplashStep.STARTING_SERVER -> "サーバーを起動しています…"
        DesktopSplashStep.READY -> "起動が完了しました"
    }
