package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

/** LMU の車両接近の開始・継続ごとの左右の自由文字列を永続化する。空白文言では本文を読み上げない。 */
public interface LmuWindowsVehicleApproachReadoutTextPreferencesRepository {
    public fun observeStartLeftReadoutText(): Flow<String>

    public suspend fun saveStartLeftReadoutText(text: String)

    public fun observeStartRightReadoutText(): Flow<String>

    public suspend fun saveStartRightReadoutText(text: String)

    public fun observeSustainedLeftReadoutText(): Flow<String>

    public suspend fun saveSustainedLeftReadoutText(text: String)

    public fun observeSustainedRightReadoutText(): Flow<String>

    public suspend fun saveSustainedRightReadoutText(text: String)
}
