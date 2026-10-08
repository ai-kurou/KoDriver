package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/** LMU のピットタイミングの読み上げ文言を永続化する。 */
interface LmuWindowsPitTimingReadoutTextPreferencesRepository {
    fun observeVirtualEnergyReadoutText(): Flow<String>

    suspend fun saveVirtualEnergyReadoutText(text: String)

    fun observeVirtualEnergyImminentReadoutText(): Flow<String>

    suspend fun saveVirtualEnergyImminentReadoutText(text: String)

    fun observeTyreWearReadoutText(): Flow<String>

    suspend fun saveTyreWearReadoutText(text: String)

    fun observeTyreWearImminentReadoutText(): Flow<String>

    suspend fun saveTyreWearImminentReadoutText(text: String)
}
