package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kurou.kodriver.core.lmuwindowsdata.datasource.SharedLmuWindowsMemorySource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsRepositoryImplTest {

    private fun makeSource(
        reader: FakeMemoryReader,
        pollingIntervalMs: Long = 16L,
        reconnectIntervalMs: Long = 1_000L,
    ) = SharedLmuWindowsMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        currentTimeMs = System::currentTimeMillis,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `reader が open 済みかつデータを読み取れるとき isConnected は true を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true, openResults = listOf(true))
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake))

        assertTrue(repo.isConnected())
    }

    @Test
    fun `reader が未 open かつ open 後にデータを読み取れるとき isConnected は true を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(true))
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake))

        assertTrue(repo.isConnected())
    }

    @Test
    fun `reader が未 open かつ open 失敗のとき isConnected は false を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(false))
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake))

        assertFalse(repo.isConnected())
    }

    @Test
    fun `reader が open 済みでもデータを読み取れないとき isConnected は false を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true, openResults = listOf(true), returnNullBuffer = true)
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake))

        assertFalse(repo.isConnected())
    }

    @Test
    fun `disconnect は reader の close を呼ぶ`() = runBlocking {
        val fake = FakeMemoryReader()
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake))

        repo.disconnect()

        assertTrue(fake.closeCalled)
    }

    @Test
    fun `reader が open 済みのときデータを emit する`() = runBlocking<Unit> {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake, pollingIntervalMs = 1))

        repo.telemetryStream().first()
    }

    @Test
    fun `未接続から open に成功するとデータを emit する`() = runBlocking<Unit> {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(true))
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake, pollingIntervalMs = 1))

        repo.telemetryStream().first()

        assertEquals(1, fake.openCallCount)
    }

    @Test
    fun `open 失敗後に再接続してデータを emit する`() = runBlocking<Unit> {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(false, true))
        val repo = LmuWindowsRepositoryImpl(
            source = makeSource(
                reader = fake,
                pollingIntervalMs = 1,
                reconnectIntervalMs = 1,
            ),
        )

        withTimeout(1_000) { repo.telemetryStream().first() }

        assertEquals(2, fake.openCallCount)
    }

    @Test
    fun `readBuffer が null の間は emit せずキャンセル時に close する`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true, returnNullBuffer = true)
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake, pollingIntervalMs = 1))
        var emitCount = 0

        val job = launch { repo.telemetryStream().collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()
        // WhileSubscribed が IO スレッドへ cancellation を伝播するまで待機
        delay(100)

        assertEquals(0, emitCount)
        assertTrue(fake.closeCalled)
    }

    @Test
    fun `フローがキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuWindowsRepositoryImpl(source = makeSource(fake, pollingIntervalMs = 1))

        val job = launch { repo.telemetryStream().collect { } }
        delay(50)
        job.cancelAndJoin()
        // WhileSubscribed が IO スレッドへ cancellation を伝播するまで待機
        delay(100)

        assertTrue(fake.closeCalled)
    }
}
