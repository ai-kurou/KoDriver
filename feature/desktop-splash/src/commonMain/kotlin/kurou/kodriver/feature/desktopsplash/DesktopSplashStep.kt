package kurou.kodriver.feature.desktopsplash

import kurou.kodriver.feature.desktopsplash.generated.resources.Res
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_initializing_modules
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_ready
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_starting_server
import org.jetbrains.compose.resources.StringResource

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
 * スプラッシュ画面に表示するフェーズ名の文字列リソース。
 *
 * 内部の識別子（[DesktopSplashStep] の名前）と表示名を分離するため、
 * 表示名はこの拡張プロパティ側に持たせる。
 */
val DesktopSplashStep.displayNameRes: StringResource
    get() =
        when (this) {
            DesktopSplashStep.INITIALIZING_MODULES -> Res.string.desktop_splash_initializing_modules
            DesktopSplashStep.STARTING_SERVER -> Res.string.desktop_splash_starting_server
            DesktopSplashStep.READY -> Res.string.desktop_splash_ready
        }
