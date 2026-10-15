package kurou.kodriver.feature.manualreadoutsetdetail

internal sealed interface ManualReadoutHotkeyConflict {
    data class OtherSet(
        val simulatorId: String,
        val setNumber: Int,
    ) : ManualReadoutHotkeyConflict

    data object DebugHotkey : ManualReadoutHotkeyConflict
}
