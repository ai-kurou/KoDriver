package kurou.kodriver.feature.manualreadoutdetail

internal sealed interface ManualReadoutHotkeyConflict {
    data class OtherSet(
        val simulatorId: String,
        val setNumber: Int,
    ) : ManualReadoutHotkeyConflict

    data object DebugHotkey : ManualReadoutHotkeyConflict
}
