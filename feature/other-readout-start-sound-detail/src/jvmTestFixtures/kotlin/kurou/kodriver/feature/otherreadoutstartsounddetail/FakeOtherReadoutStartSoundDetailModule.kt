package kurou.kodriver.feature.otherreadoutstartsounddetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:data の代わりに
 * ReadoutStartSoundPreferencesRepository の Fake 実装をバインドし、実DataStore
 * （`~/.kodriver`）への書き込みを避ける。
 */
val fakeOtherReadoutStartSoundDetailModule = module {
    single<ReadoutStartSoundPreferencesRepository> { FakeReadoutStartSoundPreferencesRepository() }
}

class FakeReadoutStartSoundPreferencesRepository : ReadoutStartSoundPreferencesRepository {
    private val flow = MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)

    override fun observeType(): Flow<ReadoutStartSoundType> = flow

    override suspend fun saveType(type: ReadoutStartSoundType) { flow.update { type } }
}
