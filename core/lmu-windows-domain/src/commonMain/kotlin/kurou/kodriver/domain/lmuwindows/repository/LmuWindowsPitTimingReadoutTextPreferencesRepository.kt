package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

/** LMU のピットタイミングの読み上げ文言を永続化する。 */
@Suppress("TooManyFunctions")
interface LmuWindowsPitTimingReadoutTextPreferencesRepository {
    fun observeVirtualEnergyReadoutText(): Flow<String>

    suspend fun saveVirtualEnergyReadoutText(text: String)

    fun observeVirtualEnergyImminentReadoutText(): Flow<String>

    suspend fun saveVirtualEnergyImminentReadoutText(text: String)

    fun observeTyreWearReadoutText(): Flow<String>

    fun observeFuelReadoutText(): Flow<String>

    suspend fun saveTyreWearReadoutText(text: String)

    suspend fun saveFuelReadoutText(text: String)

    fun observeTyreWearImminentReadoutText(): Flow<String>

    fun observeFuelImminentReadoutText(): Flow<String>

    suspend fun saveTyreWearImminentReadoutText(text: String)

    suspend fun saveFuelImminentReadoutText(text: String)
}
