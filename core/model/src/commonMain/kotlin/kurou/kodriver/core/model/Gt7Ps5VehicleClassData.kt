package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Gt7Ps5VehicleClassData(
    /** 車両カテゴリ（例: "GR3", "GRX"）。取得できない場合は空文字列。 */
    val name: String,
)
