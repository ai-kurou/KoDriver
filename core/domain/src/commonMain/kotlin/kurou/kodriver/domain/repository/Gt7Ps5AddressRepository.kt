package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5AddressRepository {
    fun gt7Ps5Address(): Flow<String?>
    suspend fun saveGt7Ps5Address(address: String)
}
