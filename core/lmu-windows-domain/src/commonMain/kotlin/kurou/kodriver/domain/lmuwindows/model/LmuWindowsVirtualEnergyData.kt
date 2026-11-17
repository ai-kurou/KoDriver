package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
public data class LmuWindowsVirtualEnergyData(
    /** バーチャルエネルギー残量割合（0.0〜1.0）。 */
    val remainingRatio: LmuWindowsVirtualEnergyRatio,
    /** Scoring の mSession 値。プラクティス・予選・レースなどセッションの切り替わり検出に使う。 */
    val session: Int = SESSION_UNKNOWN,
) {
    public companion object {
        /** 旧バージョンのサーバーなど session を配信しない接続元からのデータを表す値。 */
        public const val SESSION_UNKNOWN: Int = -1
    }
}
