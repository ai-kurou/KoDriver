package kurou.kodriver.feature.lmuwindowsconnection

import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.dsl.module

/**
 * LMU 接続状態監視（lmu-windows-connection feature）の Koin モジュール。
 *
 * 提供: LMU 接続監視に使うドメイン UseCase（LmuWindowsConnectionViewModel やバナー用チェッカーが消費）。
 * 消費（get で解決）: LmuWindowsRepository（:core:lmu-windows-data / Android は :core:data の WebSocket 実装）・
 *   SimulatorPreferencesRepository（:core:data）。
 */
val lmuWindowsConnectionModule = module {
    // ドメイン UseCase（:core:domain。get() は接続元 Repository・:core:data の Preferences Repository を解決）
    factory { CheckLmuWindowsConnectionUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
}
