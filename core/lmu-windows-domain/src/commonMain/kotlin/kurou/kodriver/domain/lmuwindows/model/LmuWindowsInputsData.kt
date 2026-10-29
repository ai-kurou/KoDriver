package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsInputsData(
    val throttle: Double,
    val brake: Double,
    val clutch: Double,
    val steering: Double,
)
