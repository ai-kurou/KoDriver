package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Simulator

/**
 * ユーザーが選択したシミュレーターを保存する Repository。
 */
interface SimulatorPreferencesRepository {
    /** 選択済みシミュレーターを購読する。初回起動など未設定時は [Simulator.LmuWindows] を流す。 */
    fun selectedSimulator(): Flow<Simulator>

    /** ユーザーが選択したシミュレーターを保存する。 */
    suspend fun saveSelectedSimulator(simulator: Simulator)
}
