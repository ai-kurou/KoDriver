package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSoundVolumeUseCaseTest {

    @Test
    fun `初期値を返す・保存済みの値を返す`() = runBlocking {
        val repo = FakeSoundVolumePreferencesRepository(initial = 80)
        val useCase = ObserveSoundVolumeUseCase(repo)

        assertEquals(80, useCase().first())

        repo.saveVolume(50)
        assertEquals(50, useCase().first())
    }
}
