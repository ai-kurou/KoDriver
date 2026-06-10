package kurou.kodriver.data.repository

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuRepositoryImplTest {

    @Test
    fun `reader が open 済みのとき isConnected は true を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuRepositoryImpl(reader = fake)

        assertTrue(repo.isConnected())
    }

    @Test
    fun `reader が未 open かつ open 成功のとき isConnected は true を返す`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(true))
        val repo = LmuRepositoryImpl(reader = fake)

        assertTrue(repo.isConnected())
    }

    @Test
    fun `reader が未 open かつ open 失敗のとき isConnected は false を返し close が呼ばれる`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(false))
        val repo = LmuRepositoryImpl(reader = fake)

        assertFalse(repo.isConnected())
        assertTrue(fake.closeCalled)
    }

    @Test
    fun `disconnect は reader の close を呼ぶ`() = runBlocking {
        val fake = FakeMemoryReader()
        val repo = LmuRepositoryImpl(reader = fake)

        repo.disconnect()

        assertTrue(fake.closeCalled)
    }

    @Test
    fun `reader が open 済みのときデータを emit する`() = runBlocking<Unit> {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuRepositoryImpl(pollingIntervalMs = 1, reader = fake)

        // first() は要素が emit されなければ NoSuchElementException を投げるため、
        // 正常に返れば emit されたことの確認になる
        repo.telemetryStream().first()
    }

    @Test
    fun `未接続から open に成功するとデータを emit する`() = runBlocking<Unit> {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(true))
        val repo = LmuRepositoryImpl(pollingIntervalMs = 1, reader = fake)

        repo.telemetryStream().first()

        assertEquals(1, fake.openCallCount)
    }

    @Test
    fun `open 失敗後に再接続してデータを emit する`() = runBlocking<Unit> {
        val fake = FakeMemoryReader(initialOpen = false, openResults = listOf(false, true))
        val repo = LmuRepositoryImpl(
            pollingIntervalMs = 1,
            reconnectIntervalMs = 1,
            reader = fake,
        )

        withTimeout(1_000) { repo.telemetryStream().first() }

        assertEquals(2, fake.openCallCount)
    }

    @Test
    fun `readBuffer が null の間は emit せずキャンセル時に close する`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true, returnNullBuffer = true)
        val repo = LmuRepositoryImpl(pollingIntervalMs = 1, reader = fake)
        var emitCount = 0

        val job = launch { repo.telemetryStream().collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount)
        assertTrue(fake.closeCalled)
    }

    @Test
    fun `フローがキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuRepositoryImpl(pollingIntervalMs = 1, reader = fake)

        val job = launch { repo.telemetryStream().collect { } }
        delay(50)
        job.cancelAndJoin()

        assertTrue(fake.closeCalled)
    }
}
