package kurou.kodriver.feature.main

/** app:shared に渡す音量警告の表示状態。 */
data class LowVolumeWarningUiState(
    val deviceVolume: Int? = null,
    val soundVolume: Int? = null,
) {
    val isVisible: Boolean get() = deviceVolume != null || soundVolume != null
}
