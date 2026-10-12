package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/** LMU のタイヤ温度警告の読み上げ文言を永続化する。 */
interface LmuWindowsTyreTemperatureReadoutTextPreferencesRepository {
    fun observeOverheatReadoutText(): Flow<String>

    suspend fun saveOverheatReadoutText(text: String)

    fun observeColdReadoutText(): Flow<String>

    suspend fun saveColdReadoutText(text: String)
}
