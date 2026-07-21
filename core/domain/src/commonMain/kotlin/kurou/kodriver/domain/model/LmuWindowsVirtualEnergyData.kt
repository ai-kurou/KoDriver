package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsVirtualEnergyData(
    /** バーチャルエナジー残量割合（0.0〜1.0）。 */
    val remainingRatio: Double,
    /** Scoring の mSession 値。プラクティス・予選・レースなどセッションの切り替わり検出に使う。 */
    val session: Int = SESSION_UNKNOWN,
) {
    companion object {
        /** 旧バージョンのサーバーなど session を配信しない接続元からのデータを表す値。 */
        const val SESSION_UNKNOWN: Int = -1
    }
}
