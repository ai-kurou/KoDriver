package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.Gt7Ps5AddressRepository

internal class FakeGt7Ps5AddressRepository(initial: String? = null) : Gt7Ps5AddressRepository {
    private val flow = MutableStateFlow(initial)
    override fun gt7Ps5Address(): Flow<String?> = flow
    override suspend fun saveGt7Ps5Address(address: String) { flow.value = address }
}
