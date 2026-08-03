package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AceWindowsFuelRepositoryImplTest {
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
        runTest {
            val fake = FakeSharedMemoryReader(initialOpen = true)
            val repo = AceWindowsFuelRepositoryImpl(source = makeSource(fake))

            repo.fuelStream().first()
        }

    @Test
    fun `reader が open できるとき isConnected は true を返す`() =
        runTest {
            val fake = FakeSharedMemoryReader(openResults = listOf(true))
            val repo = AceWindowsFuelRepositoryImpl(source = makeSource(fake))

            assertTrue(repo.isConnected())
        }

    @Test
    fun `reader が open できないとき isConnected は false を返す`() =
        runTest {
            val fake = FakeSharedMemoryReader(openResults = listOf(false))
            val repo = AceWindowsFuelRepositoryImpl(source = makeSource(fake))

            assertFalse(repo.isConnected())
        }
}
