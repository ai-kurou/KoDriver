package kurou.kodriver.data.repository

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
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
        val fake = FakeMemoryReader(initialOpen = false, openResult = true)
        val repo = LmuRepositoryImpl(reader = fake)

        assertTrue(repo.isConnected())
    }

    @Test
    fun `reader が未 open かつ open 失敗のとき isConnected は false を返し close が呼ばれる`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = false, openResult = false)
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
    fun `フローがキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val fake = FakeMemoryReader(initialOpen = true)
        val repo = LmuRepositoryImpl(pollingIntervalMs = 1, reader = fake)

        val job = launch { repo.telemetryStream().collect { } }
        delay(50)
        job.cancelAndJoin()

        assertTrue(fake.closeCalled)
    }
}
