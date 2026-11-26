package kurou.kodriver.feature.serverconnection

import kurou.kodriver.domain.serverconnection.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.serverconnection.usecase.ObserveKoDriverServerConnectionUseCase
import kurou.kodriver.domain.serverconnection.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * KoDriver サーバー接続確認（server-connection feature）の Koin モジュール。
 *
 * 提供: ServerConnectionViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: ServerVersionRepository・ServerIpPreferencesRepository・
 *   SimulatorPreferencesRepository（:core:data）、named("appVersion") のバージョン定数
 *   （app エントリーポイントで登録）。
 */
public val serverConnectionModule: Module =
    module {
        // ViewModel（get(named "appVersion") は app エントリーポイントで束ねるバージョン定数を解決）
        viewModel { ServerConnectionViewModel(get(), get(named("appVersion"))) }

        // ドメイン UseCase（:core:domain。get() は :core:data の Repository を解決）
        factory { FetchServerVersionUseCase(get()) }
        factory { ObserveServerIpUseCase(get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveKoDriverServerConnectionUseCase(get(), get(), get()) }
    }
