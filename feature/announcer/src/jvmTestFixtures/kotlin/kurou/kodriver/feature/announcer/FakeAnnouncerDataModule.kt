package kurou.kodriver.feature.announcer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository
import org.koin.dsl.module

val fakeAnnouncerDataModule = module {
    single<ProximityRepository> { FakeProximityRepository() }
    single<TextToSpeechEngine> { NoOpTextToSpeechEngine() }
}

class FakeProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

class NoOpTextToSpeechEngine : TextToSpeechEngine {
    override fun speak(text: String) = Unit
    override fun stop() = Unit
}
