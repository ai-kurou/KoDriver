package kurou.kodriver.data.lowvolumewarning

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DismissedVolumes
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryLowVolumeWarningDismissalRepositoryTest {
    @Test
    fun `初期値は未dismiss状態で保存値を購読できる`() =
        runTest {
            val repository = InMemoryLowVolumeWarningDismissalRepository()

            assertEquals(DismissedVolumes(), repository.observeDismissedVolumes().first())
            repository.saveDismissedVolumes(DismissedVolumes(20, 60))
            assertEquals(DismissedVolumes(20, 60), repository.observeDismissedVolumes().first())
        }
}
