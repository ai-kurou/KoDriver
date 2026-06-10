package kurou.kodriver.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.data.datasource.SharedLmuMemorySource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuRepositoryImplTest {

    private fun makeSource(
        reader: FakeMemoryReader,
        pollingIntervalMs: Long = 16L,
        reconnectIntervalMs: Long = 1_000L,
    ) = SharedLmuMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `reader が open 済みのとき isConnected は true を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuRepositoryImpl(source = makeSource(fake))

        assertTrue(repo.isConnected())
    }

    @Test
    fun `reader が未 open かつ open 成功のとき isConnected は true を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = false, openResult = true)
        val repo = LmuRepositoryImpl(source = makeSource(fake))

        assertTrue(repo.isConnected())
    }

    @Test
    fun `reader が未 open かつ open 失敗のとき isConnected は false を返し close が呼ばれる`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = false, openResult = false)
        val repo = LmuRepositoryImpl(source = makeSource(fake))

        assertFalse(repo.isConnected())
        assertTrue(fake.closeCalled)
    }

    @Test
    fun `disconnect は reader の close を呼ぶ`() = runBlocking {
        val fake = FakeMemoryReader()
        val repo = LmuRepositoryImpl(source = makeSource(fake))

        repo.disconnect()

        assertTrue(fake.closeCalled)
    }

    @Test
    fun `reader が open 済みのときデータを emit する`() = runBlocking<Unit> {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuRepositoryImpl(source = makeSource(fake, pollingIntervalMs = 1))

        repo.telemetryStream().first()
    }

    @Test
    fun `フローがキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuRepositoryImpl(source = makeSource(fake, pollingIntervalMs = 1))

        val job = launch { repo.telemetryStream().collect { } }
        delay(50)
        job.cancelAndJoin()
        // WhileSubscribed が IO スレッドへ cancellation を伝播するまで待機
        delay(100)

        assertTrue(fake.closeCalled)
    }
}
