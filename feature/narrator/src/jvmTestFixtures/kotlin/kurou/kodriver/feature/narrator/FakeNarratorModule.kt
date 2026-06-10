package kurou.kodriver.feature.narrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.ProximityRepository
import org.koin.dsl.module

val fakeNarratorDataModule = module {
    single<ProximityRepository> { FakeProximityRepository() }
    single<FlagRepository> { FakeFlagRepository() }
    single<SoundPlayer> { NoOpSoundPlayer() }
}

class FakeProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

class FakeFlagRepository : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = emptyFlow()
}

class NoOpSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override suspend fun play(bytes: ByteArray) = Unit
}
