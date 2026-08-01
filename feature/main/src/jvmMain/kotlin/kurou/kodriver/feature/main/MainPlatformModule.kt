package kurou.kodriver.feature.main

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * main feature のプラットフォーム別バインド（expect は MainModule.kt）。
 * LMU / ACE 接続バナー用の LmuBannerConnectionChecker / AceBannerConnectionChecker を
 * プラットフォーム別に提供する（JVM=共有メモリ経由、Android=KoDriver サーバーへの疎通確認、
 * js/wasmJs=未提供の空モジュール）。
 */
actual val mainPlatformModule: Module =
    module {
    factory<LmuBannerConnectionChecker> { LmuWindowsBannerConnectionChecker(get()) }
    factory<AceBannerConnectionChecker> { AceWindowsBannerConnectionChecker(get()) }
}
