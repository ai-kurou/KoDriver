package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AceWindowsStatusData(
    val status: AceWindowsStatusType,
)

// ACE shared memory の graphics.status に対応する ACEVO_STATUS の値。
// docs/ace-windows-telemetry.md の ACEVO_STATUS 一覧に基づく。
@Serializable
enum class AceWindowsStatusType(
    val rawValue: Int,
) {
    OFF(0),
    REPLAY(1),
    LIVE(2),
    PAUSE(3),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): AceWindowsStatusType = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}
