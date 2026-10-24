package kurou.kodriver.domain.model

/** nullでない側が警告対象の音量とその現在値を表す。 */
data class LowVolumeWarning(
    val deviceVolume: Int?,
    val soundVolume: Int?,
)
