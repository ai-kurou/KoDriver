package kurou.kodriver.feature.announcer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository
import org.koin.dsl.module

val fakeAnnouncerDataModule = module {
    single<ProximityRepository> { FakeProximityRepository() }
    single<SoundPlayer> { NoOpSoundPlayer() }
}

class FakeProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

internal class NoOpSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override fun play(bytes: ByteArray) = Unit
}
