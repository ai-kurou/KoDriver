package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsCarGapData(
    /** 前方車両との時間差（秒）。共有メモリの生の値。車両不在時の値の意味（0か負値か）は実機未確認。 */
    val timeGapCarAheadSeconds: Double,
    /** 後方車両との時間差（秒）。共有メモリの生の値。車両不在時の値の意味（0か負値か）は実機未確認。 */
    val timeGapCarBehindSeconds: Double,
    /** 前順位車両との時間差（秒）。共有メモリの生の値。車両不在時の値の意味（0か負値か）は実機未確認。 */
    val timeGapPlaceAheadSeconds: Double,
    /** 後順位車両との時間差（秒）。共有メモリの生の値。車両不在時の値の意味（0か負値か）は実機未確認。 */
    val timeGapPlaceBehindSeconds: Double,
    /** 全体の前方車両との秒差。対象車両なし・推定ラップタイム無効時は null。Scoring の位置関係から算出。 */
    val overallGapAheadSeconds: Double? = null,
    /** 全体の後方車両との秒差。対象車両なし・推定ラップタイム無効時は null。Scoring の位置関係から算出。 */
    val overallGapBehindSeconds: Double? = null,
)
