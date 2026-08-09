package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AceWindowsFlagData(
    val flag: AceWindowsFlagType,
)

// ACE shared memory の graphics.flag（自車提示フラッグ）に対応する ACEVO_FLAG_TYPE の値。
// docs/ace-windows-telemetry.md の ACEVO_FLAG_TYPE 一覧に基づく。
@Serializable
enum class AceWindowsFlagType(
    val rawValue: Int,
) {
    NO_FLAG(0),
    WHITE_FLAG(1),
    GREEN_FLAG(2),
    RED_FLAG(3),
    BLUE_FLAG(4),
    YELLOW_FLAG(5),
    BLACK_FLAG(6),
    BLACK_WHITE_FLAG(7),
    CHECKERED_FLAG(8),
    ORANGE_CIRCLE_FLAG(9),
    RED_YELLOW_STRIPES_FLAG(10),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): AceWindowsFlagType = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}
