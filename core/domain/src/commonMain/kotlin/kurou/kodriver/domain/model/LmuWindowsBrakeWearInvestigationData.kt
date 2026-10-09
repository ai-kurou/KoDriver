package kurou.kodriver.domain.model

/**
 * ブレーキ摩耗の実機調査用に、LMU 内蔵 REST API の生の配列値をそのまま保持する。
 *
 * どちらの値がブレーキ摩耗を表すのかを確認する目的の暫定データで、単位や並びの解釈は行わない。
 * 取得できなかった（LMU 未起動・非対応環境など）項目は null。
 */
data class LmuWindowsBrakeWearInvestigationData(
    /** `GET /rest/garage/UIScreen/RepairAndRefuel` の `wearables.brakes`。 */
    val wearablesBrakes: List<Double>? = null,
    /** `GET /rest/garage/brakeinfo`。 */
    val brakeInfo: List<Double>? = null,
)
