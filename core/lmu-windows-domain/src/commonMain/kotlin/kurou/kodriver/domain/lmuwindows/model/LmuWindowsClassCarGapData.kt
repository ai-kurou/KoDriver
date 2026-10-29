package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

/**
 * 同じクラスの車両とのコース上の位置関係から求めた時間ギャップ。
 * 周回遅れ・周回先行の車両も前後の対象に含む（TinyPedal と同じ方式）。
 */
@Serializable
data class LmuWindowsClassCarGapData(
    /** 前方の最も近い同クラス車両との秒差。対象車両なし・推定ラップタイム無効時は null。 */
    val gapAheadSeconds: Double?,
    /** 後方の最も近い同クラス車両との秒差。対象車両なし・推定ラップタイム無効時は null。 */
    val gapBehindSeconds: Double?,
    /** 自車のクラス内順位（1始まり）。算出不可は null。classPosition == 1 でクラス内トップ。 */
    val classPosition: Int? = null,
    /** 自車を含む同クラスの車両数。算出不可は null。classPosition == classVehicleCount で最後尾（両方が非 null の場合）。 */
    val classVehicleCount: Int? = null,
)
