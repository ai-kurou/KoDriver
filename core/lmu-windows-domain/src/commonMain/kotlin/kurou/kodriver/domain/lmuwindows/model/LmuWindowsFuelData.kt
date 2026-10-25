package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class LmuWindowsFuelData(
    val currentLiters: LmuWindowsFuelUnit,
    val capacityLiters: LmuWindowsFuelUnit,
    // Android での実受信時刻。サーバーとの時刻同期や通信形式には依存しない。
    @Transient val receivedAtMs: Long? = null,
)
