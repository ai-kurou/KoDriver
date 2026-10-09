package kurou.kodriver.core.lmuwindowsrestapidata.dto

import kotlinx.serialization.Serializable

/**
 * `GET /rest/garage/UIScreen/RepairAndRefuel` のレスポンスのうち、ブレーキ摩耗の取得に使う部分
 * （docs/lmu-windows-rest-api.md 参照）。他のキーは無視する。
 */
@Serializable
internal data class RepairAndRefuelResponseDto(
    val wearables: WearablesDto? = null,
)

/**
 * 摩耗・状態の配列群。`brakes` は 4 輪分（FL, FR, RL, RR）の残り厚さ（単位: meters）。
 */
@Serializable
internal data class WearablesDto(
    val brakes: List<Double>? = null,
)
