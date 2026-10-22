package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test

class AceWindowsVehicleApproachRepositoryImplTest {
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
            val repo = AceWindowsVehicleApproachRepositoryImpl(source = makeSource(fake))

            repo.vehicleApproachStream().first()
        }
}

private class FakeSharedMemoryReader(
    initialOpen: Boolean = false,
) : SharedMemoryReader {
    private var opened = initialOpen

    override fun open(): Boolean {
        opened = true
        return true
    }

    override fun readBuffer(): ByteBuffer? =
        if (opened) {
            ByteBuffer.allocate(8_192).order(ByteOrder.LITTLE_ENDIAN)
        } else {
            null
        }

    override fun isOpen(): Boolean = opened

    override fun close() {
        opened = false
    }
}
