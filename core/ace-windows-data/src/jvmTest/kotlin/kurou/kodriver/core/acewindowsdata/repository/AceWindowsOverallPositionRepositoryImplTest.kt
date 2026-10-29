package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.windowssharedmemory.datasource.FakeWindowsSharedMemoryReader
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsOverallPositionRepositoryImplTest {
    private fun makeSource(
        reader: FakeWindowsSharedMemoryReader,
        pollingIntervalMs: Long = 1L,
    ) = AceWindowsGraphicsSharedMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = 1L,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `reader が open 済みのときデータを emit する`() =
        runTest {
            val fake =
                FakeWindowsSharedMemoryReader(
                    initialOpen = true,
                    bufferSize = 8_192,
                    configureBuffer = {
                        it.putInt(2388, 2)
                        it.putInt(2392, 20)
                    },
                )
            val repo = AceWindowsOverallPositionRepositoryImpl(source = makeSource(fake))

            assertEquals(AceWindowsOverallPositionData(2, 20), repo.overallPositionStream().first())
        }
}
