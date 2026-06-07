package kurou.kodriver.data.datasource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedMemoryReaderTest {

    private fun reader(
        fake: FakeKernel32Ext = FakeKernel32Ext(),
        isWindows: Boolean = true,
    ) = SharedMemoryReader(
        segmentName = "TEST",
        sizeBytes = 1024,
        isWindows = isWindows,
        kernel32 = fake,
    )

    @Test
    fun `非 Windows 環境では open は false を返す`() {
        val r = reader(isWindows = false)

        assertFalse(r.open())
        assertFalse(r.isOpen())
    }

    @Test
    fun `OpenFileMappingA が null を返すとき open は false を返す`() {
        val fake = FakeKernel32Ext(openFileMappingResult = null)
        val r = reader(fake)

        assertFalse(r.open())
        assertFalse(r.isOpen())
    }

    @Test
    fun `MapViewOfFile が null を返すとき CloseHandle を呼び open は false を返す`() {
        val fake = FakeKernel32Ext(mapViewOfFileResult = null)
        val r = reader(fake)

        assertFalse(r.open())
        assertFalse(r.isOpen())
        assertEquals(1, fake.closeHandleCallCount)
    }

    @Test
    fun `open 成功時に true を返し isOpen が true になる`() {
        val r = reader()

        assertTrue(r.open())
        assertTrue(r.isOpen())
    }

    @Test
    fun `open 前の readBuffer は null を返す`() {
        val r = reader()

        assertNull(r.readBuffer())
    }

    @Test
    fun `close は UnmapViewOfFile と CloseHandle を呼ぶ`() {
        val fake = FakeKernel32Ext()
        val r = reader(fake)
        r.open()

        r.close()

        assertTrue(fake.unmapViewOfFileCalled)
        assertEquals(1, fake.closeHandleCallCount)
        assertFalse(r.isOpen())
    }

    @Test
    fun `close 後に再度 close を呼んでも UnmapViewOfFile と CloseHandle は呼ばれない`() {
        val fake = FakeKernel32Ext()
        val r = reader(fake)
        r.open()
        r.close()

        r.close()

        assertEquals(1, fake.closeHandleCallCount)
    }
}
