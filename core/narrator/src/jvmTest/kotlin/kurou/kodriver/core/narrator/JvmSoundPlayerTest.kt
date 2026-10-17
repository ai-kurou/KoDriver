package kurou.kodriver.core.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JvmSoundPlayerTest {
    private val audio = FakeJvmAudioBackend()
    private val errors = mutableListOf<Throwable>()

    private fun player(dispatcher: CoroutineDispatcher): JvmSoundPlayer = JvmSoundPlayer(audio, dispatcher, errors::add)

    @Test
    fun `標準コンストラクタの初期状態は停止中で不正なWAVでも例外を伝播しない`() =
        runTest {
            val player = JvmSoundPlayer()
            assertFalse(player.isPlaying)

            player.play(ByteArray(0))

            assertFalse(player.isPlaying)
        }

    @Test
    fun `音声と末尾の無音を順番に書き込み完了後に停止して解放する`() =
        runTest {
            val player = player(StandardTestDispatcher(testScheduler))
            val bytes = byteArrayOf(9, 8)
            val playing = mutableListOf<Boolean>()
            audio.line.onWrite = { playing.add(player.isPlaying) }

            player.play(bytes)

            assertSame(bytes, audio.openedBytes)
            assertSame(audio.format, audio.requestedFormat)
            assertSame(audio.format, audio.line.openedFormat)
            assertEquals(
                listOf("open", "supportsGain", "getGain", "gain", "start", "write", "write", "drain", "stop", "close"),
                audio.line.calls,
            )
            assertContentEquals(audio.samples, audio.line.writes[0])
            assertContentEquals(ByteArray(600), audio.line.writes[1])
            assertEquals(listOf(true, true), playing)
            assertTrue(audio.streamClosed)
            assertFalse(player.isPlaying)
            assertTrue(errors.isEmpty())
        }

    @Test
    fun `バッファを超える音声も最後の有効長だけを書き込む`() =
        runTest {
            audio.samples = ByteArray(8200) { (it % 100).toByte() }

            player(StandardTestDispatcher(testScheduler)).play(byteArrayOf(1))

            assertEquals(listOf(8192, 8, 600), audio.line.writes.map { it.size })
            assertContentEquals(
                audio.samples,
                audio.line.writes
                    .take(2)
                    .flatMap { it.toList() }
                    .toByteArray(),
            )
        }

    @Test
    fun `空の音声でもフォーマットに応じた無音を追記してdrainする`() =
        runTest {
            audio.samples = ByteArray(0)

            player(StandardTestDispatcher(testScheduler)).play(byteArrayOf(1))

            assertContentEquals(ByteArray(600), audio.line.writes.single())
            assertTrue("drain" in audio.line.calls)
        }

    @Test
    fun `デバイスが停止中なら再生処理中でもisPlayingはfalseを返す`() =
        runTest {
            val player = player(StandardTestDispatcher(testScheduler))
            val playing = mutableListOf<Boolean>()
            audio.line.onWrite = {
                audio.line.running = false
                playing.add(player.isPlaying)
            }

            player.play(byteArrayOf(1))

            assertEquals(listOf(false, false), playing)
        }

    @Test
    fun `デコード失敗を記録して再生デバイスに触れない`() =
        runTest {
            audio.failureAt = "decode"
            val player = player(StandardTestDispatcher(testScheduler))

            player.play(byteArrayOf(1))

            assertEquals(listOf<Throwable>(audio.failure), errors)
            assertTrue(audio.line.calls.isEmpty())
            assertFalse(player.isPlaying)
        }

    @Test
    fun `デバイス取得失敗でも音声ストリームを閉じる`() =
        runTest {
            audio.failureAt = "getLine"

            player(StandardTestDispatcher(testScheduler)).play(byteArrayOf(1))

            assertTrue(audio.streamClosed)
            assertEquals(listOf<Throwable>(audio.failure), errors)
            assertTrue(audio.line.calls.isEmpty())
        }

    @Test
    fun `open失敗でもデバイスとストリームを閉じる`() =
        runTest {
            audio.line.failures = setOf("open")

            player(StandardTestDispatcher(testScheduler)).play(byteArrayOf(1))

            assertEquals(listOf("open", "close"), audio.line.calls)
            assertTrue(audio.streamClosed)
            assertEquals(listOf<Throwable>(audio.line.failure), errors)
        }

    @Test
    fun `音量設定とstartの失敗でも停止と解放を行う`() =
        runTest {
            for (stage in listOf("gain", "start")) {
                audio.line.calls.clear()
                audio.streamClosed = false
                audio.line.failures = setOf(stage)
                val player = player(StandardTestDispatcher(testScheduler))

                player.play(byteArrayOf(1))

                assertEquals(listOf("stop", "close"), audio.line.calls.takeLast(2))
                assertTrue(audio.streamClosed)
                assertFalse(player.isPlaying)
            }
            assertEquals(2, errors.size)
        }

    @Test
    fun `読み取り失敗でも停止と解放を行い再生状態を戻す`() =
        runTest {
            audio.failureAt = "read"
            val player = player(StandardTestDispatcher(testScheduler))

            player.play(byteArrayOf(1))

            assertEquals(listOf("stop", "close"), audio.line.calls.takeLast(2))
            assertTrue(audio.streamClosed)
            assertFalse(player.isPlaying)
            assertEquals(audio.failure.message, assertIs<IOException>(errors.single()).message)
        }

    @Test
    fun `書き込みとdrainの失敗でも停止と解放を行う`() =
        runTest {
            for (stage in listOf("write", "drain")) {
                audio.line.calls.clear()
                audio.streamClosed = false
                audio.line.failures = setOf(stage)
                val player = player(StandardTestDispatcher(testScheduler))

                player.play(byteArrayOf(1))

                assertEquals(listOf("stop", "close"), audio.line.calls.takeLast(2))
                assertFalse(player.isPlaying)
            }
            assertEquals(2, errors.size)
        }

    @Test
    fun `stopとcloseの失敗でも再生状態を解除しストリームを閉じる`() =
        runTest {
            for (stage in listOf("stop", "close")) {
                audio.line.calls.clear()
                audio.streamClosed = false
                audio.line.failures = setOf(stage)
                val player = player(StandardTestDispatcher(testScheduler))

                player.play(byteArrayOf(1))

                assertEquals(listOf("stop", "close"), audio.line.calls.takeLast(2))
                assertTrue(audio.streamClosed)
                assertFalse(player.isPlaying)
            }
            assertEquals(2, errors.size)
        }

    @Test
    fun `ストリームのclose失敗も記録して再生状態を解除する`() =
        runTest {
            audio.failureAt = "streamClose"
            val player = player(StandardTestDispatcher(testScheduler))

            player.play(byteArrayOf(1))

            assertEquals(listOf<Throwable>(audio.failure), errors)
            assertFalse(player.isPlaying)
        }

    @Test
    fun `キャンセルはエラーとして記録せず停止と解放を行う`() =
        runTest {
            val player = player(StandardTestDispatcher(testScheduler))
            val playback = launch { player.play(byteArrayOf(1)) }
            audio.onRead = { playback.cancel() }

            runCurrent()
            playback.join()

            assertTrue(playback.isCancelled)
            assertTrue(audio.line.writes.isEmpty())
            assertFalse("drain" in audio.line.calls)
            assertEquals(listOf("stop", "close"), audio.line.calls.takeLast(2))
            assertTrue(audio.streamClosed)
            assertFalse(player.isPlaying)
            assertTrue(errors.isEmpty())
        }

    @Test
    fun `空音声の読取後にキャンセルしても無音とdrainを実行しない`() =
        runTest {
            audio.samples = ByteArray(0)
            val player = player(StandardTestDispatcher(testScheduler))
            val playback = launch { player.play(byteArrayOf(1)) }
            audio.onRead = { playback.cancel() }

            runCurrent()
            playback.join()

            assertTrue(playback.isCancelled)
            assertTrue(audio.line.writes.isEmpty())
            assertFalse("drain" in audio.line.calls)
            assertFalse(player.isPlaying)
            assertTrue(errors.isEmpty())
        }

    @Test
    fun `解放が失敗してもキャンセルを別の例外に置き換えない`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            audio.line.failures = setOf("stop", "close")
            audio.onRead = { throw cancellation }
            val player = player(StandardTestDispatcher(testScheduler))

            val thrown = assertFailsWith<CancellationException> { player.play(byteArrayOf(1)) }

            assertEquals(cancellation.message, thrown.message)
            assertEquals(listOf("stop", "close"), audio.line.calls.takeLast(2))
            assertTrue(audio.streamClosed)
            assertFalse(player.isPlaying)
            assertTrue(errors.isEmpty())
        }
}
