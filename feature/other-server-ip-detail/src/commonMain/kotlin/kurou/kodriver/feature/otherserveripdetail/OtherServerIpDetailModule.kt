package kurou.kodriver.feature.otherserveripdetail

import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 接続先サーバー IP 設定ダイアログ（other-server-ip-detail feature）の Koin モジュール。
 *
 * 提供: OtherServerIpDetailViewModel と、それが使うドメイン UseCase、接続確認用の
 *   ServerConnectivityChecker（プラットフォーム実装を expect/actual で解決）。
 * 消費（get で解決）: ServerIpPreferencesRepository（:core:data で登録）。
 */
val otherServerIpDetailModule = module {
    // ViewModel
    viewModelOf(::OtherServerIpDetailViewModel)

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factory { ObserveServerIpUseCase(get()) }
    factory { SaveServerIpUseCase(get()) }

    // feature 固有: サーバー疎通確認（プラットフォーム別実装）
    factory<ServerConnectivityChecker> { createServerConnectivityChecker() }
}
