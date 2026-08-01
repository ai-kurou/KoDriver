package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kotlin.test.Test

class AceWindowsFlagRepositoryImplTest {

    private fun makeSource(
        reader: FakeSharedMemoryReader,
        pollingIntervalMs: Long = 1L,
    ) = AceWindowsGraphicsSharedMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = 1L,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `reader が open 済みのときデータを emit する`() =
        runBlocking<Unit> {
        val fake = FakeSharedMemoryReader(initialOpen = true)
        val repo = AceWindowsFlagRepositoryImpl(source = makeSource(fake))

        repo.flagStream().first()
    }
}
