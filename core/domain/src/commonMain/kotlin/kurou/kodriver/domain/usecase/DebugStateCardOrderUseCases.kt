package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * デバッグ状態カードの並び順を、シミュレーター固有のカードキー [K] と保存名（[Enum.name]）の間で相互変換して扱う。
 *
 * @param keys 対象シミュレーターのカードキー一覧。保存済みの名前のうち、ここに存在しないもの（削除済み項目）は読み出し時に除外する。
 */
class DebugStateCardOrderUseCases<K : Enum<K>>(
    private val keys: List<K>,
    val observeCardOrder: ObserveDebugStateCardOrderUseCase,
    val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    val saveCardOrder: SaveDebugStateCardOrderUseCase,
) {
    fun observeKeys(): Flow<List<K>> =
        observeCardOrder().map { order ->
            order.mapNotNull { name -> keys.find { it.name == name } }
        }

    suspend fun saveKeys(order: List<K>) = saveCardOrder(order.map { it.name })
}
