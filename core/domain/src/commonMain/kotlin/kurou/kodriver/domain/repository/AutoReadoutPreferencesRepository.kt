package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * 読み上げ一覧のトップレベル設定を保存する Repository。
 *
 * simulator 引数には [kurou.kodriver.domain.model.Simulator.id] を渡す。
 * キーの有効状態と並び順はシミュレーターごとに独立して保存される。
 */
interface AutoReadoutPreferencesRepository {
    /** 指定シミュレーターの読み上げ有効状態を購読する。未保存のキーは domain のデフォルト値で補完する。 */
    fun observeAutoReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>>

    /** 指定シミュレーターの読み上げ有効状態を 1 件保存する。 */
    suspend fun saveAutoReadoutEnabledState(
        simulator: String,
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    /** 指定シミュレーターの読み上げ一覧表示順を購読する。未保存または欠落キーは domain の既定順で補完する。 */
    fun observeAutoReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>>

    /** 指定シミュレーターの読み上げ一覧表示順を保存する。 */
    suspend fun saveAutoReadoutOrder(
        simulator: String,
        order: List<ReadoutItemKey>,
    )
}
