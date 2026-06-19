package kurou.kodriver.feature.otherreadoutstartsounddetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundRepository

internal class FakeReadoutStartSoundRepository(
    initialType: ReadoutStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
) : ReadoutStartSoundRepository {
    private val state = MutableStateFlow(initialType)

    override fun observeType(): Flow<ReadoutStartSoundType> = state

    override suspend fun saveType(type: ReadoutStartSoundType) {
        state.update { type }
    }
}
