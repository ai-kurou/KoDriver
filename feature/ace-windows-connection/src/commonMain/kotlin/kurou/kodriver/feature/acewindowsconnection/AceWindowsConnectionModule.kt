package kurou.kodriver.feature.acewindowsconnection

import org.koin.dsl.module

/**
 * ACE (Assetto Corsa EVO) Windows版 接続状態監視（ace-windows-connection feature）の Koin モジュール。
 *
 * 現時点ではモジュールの雛形のみで中身は未実装。ViewModel・UseCase の追加は別 PR で行う
 * （`core:ace-windows-data` 側の接続判定整備が前提のため）。
 */
val aceWindowsConnectionModule = module { }
