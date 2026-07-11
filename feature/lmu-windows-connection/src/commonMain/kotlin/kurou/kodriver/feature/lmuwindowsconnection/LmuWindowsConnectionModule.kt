package kurou.kodriver.feature.lmuwindowsconnection

import org.koin.dsl.module

/**
 * このモジュールが提供していたドメイン UseCase は共有 UseCase モジュール
 * （sharedUseCaseModule）へ集約された。将来 feature 固有の DI を追加する余地として残している。
 */
val lmuWindowsConnectionModule = module {
}
