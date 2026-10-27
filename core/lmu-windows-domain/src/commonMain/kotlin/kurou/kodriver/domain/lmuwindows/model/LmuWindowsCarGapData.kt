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
)
