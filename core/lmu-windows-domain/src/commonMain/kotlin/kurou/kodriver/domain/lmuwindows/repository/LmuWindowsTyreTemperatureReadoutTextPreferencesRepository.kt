package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

/** LMU のタイヤ温度警告の読み上げ文言を永続化する。 */
public interface LmuWindowsTyreTemperatureReadoutTextPreferencesRepository {
    public fun observeOverheatReadoutText(): Flow<String>

    public suspend fun saveOverheatReadoutText(text: String)

    public fun observeColdReadoutText(): Flow<String>

    public suspend fun saveColdReadoutText(text: String)
}
