package kurou.kodriver.domain.model

/** 警告を閉じた時点の音量。nullは未閉鎖を表す。 */
data class DismissedVolumes(
    val deviceVolume: Int? = null,
    val soundVolume: Int? = null,
)
