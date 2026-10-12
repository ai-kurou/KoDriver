package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/** LMU の車両接近の開始・継続ごとの左右の自由文字列を永続化する。空白文言では本文を読み上げない。 */
interface LmuWindowsVehicleApproachReadoutTextPreferencesRepository {
    fun observeStartLeftReadoutText(): Flow<String>

    suspend fun saveStartLeftReadoutText(text: String)

    fun observeStartRightReadoutText(): Flow<String>

    suspend fun saveStartRightReadoutText(text: String)

    fun observeSustainedLeftReadoutText(): Flow<String>

    suspend fun saveSustainedLeftReadoutText(text: String)

    fun observeSustainedRightReadoutText(): Flow<String>

    suspend fun saveSustainedRightReadoutText(text: String)
}
