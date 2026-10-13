package kurou.kodriver.domain.model

/**
 * 読み上げ順序（優先度）の初期値。一覧画面と Narrator が同じ実効順序を使うため、`:core:domain` に置く。
 * 保存済みの順序が無い場合の順序であり、保存済み順序との突き合わせは `ResolveAutoReadoutOrderUseCase` が行う。
 */
fun defaultAutoReadoutOrder(simulator: Simulator): List<ReadoutItemKey> =
    when (simulator) {
        is Simulator.LmuWindows -> lmuWindowsDefaultAutoReadoutOrder()
        is Simulator.Gt7Ps5 -> gt7Ps5DefaultAutoReadoutOrder()
        is Simulator.AceWindows -> aceWindowsDefaultAutoReadoutOrder()
    }
