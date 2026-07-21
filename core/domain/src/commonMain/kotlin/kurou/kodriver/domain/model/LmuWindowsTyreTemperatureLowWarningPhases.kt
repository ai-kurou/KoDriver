package kurou.kodriver.domain.model

// 低温警告の対象として選択可能なフェーズ全体。Repository実装のsave時の書き込みキー空間と、
// ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseのデフォルト値の両方がこの一箇所を参照する。
//
// 他の項目のデフォルト有効状態（readoutEnabledStateDefaults等）は対応するObserve*UseCaseファイル内の
// private valとして閉じているが、この値はRepository実装（core:data、明示的なtrue/false書き込みの
// キー空間として使用）からも参照する必要があるため、両モジュールから見えるdomain/model層に置いている。
val lmuWindowsTyreTemperatureLowWarningSelectablePhases: Set<SessionPhase> = setOf(
    SessionPhase.GARAGE,
    SessionPhase.WARM_UP,
    SessionPhase.GRID_WALK,
    SessionPhase.FORMATION,
)

val lmuWindowsTyreTemperatureLowWarningDefaultPhases: Set<SessionPhase> =
    lmuWindowsTyreTemperatureLowWarningSelectablePhases - SessionPhase.GARAGE
