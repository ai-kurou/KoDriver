package kurou.kodriver.feature.desktopsplash

import org.koin.dsl.module

/**
 * :feature:desktop-splash の Koin モジュール。
 *
 * [DesktopSplashProgress] を単一インスタンスとして提供し、main.kt 側の
 * 起動処理とスプラッシュ画面の双方が同じ状態を共有できるようにする。
 */
val desktopSplashModule = module {
    single { DesktopSplashProgress() }
}
