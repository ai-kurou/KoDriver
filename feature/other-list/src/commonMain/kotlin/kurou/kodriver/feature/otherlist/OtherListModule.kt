package kurou.kodriver.feature.otherlist

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * その他一覧画面（other-list feature）の Koin モジュール。
 *
 * 提供: OtherListViewModel。
 * 消費（get で解決）: OtherListViewModel が使う UseCase 群（:core:domain。実体の Repository は
 *   :core:data で登録）。アプリバージョンはビルド生成値を直接渡す。
 */
val otherListModule =
    module {
    // ViewModel
    viewModel {
        OtherListViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            currentAppVersion(),
            currentAppVersionLabel(),
        )
    }
}
