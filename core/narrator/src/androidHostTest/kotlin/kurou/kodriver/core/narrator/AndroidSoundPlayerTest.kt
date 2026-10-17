@file:Suppress("FunctionNaming")
@file:OptIn(ExperimentalCoroutinesApi::class)

package kurou.kodriver.core.narrator

import android.content.Context
import android.media.SoundPool
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class AndroidSoundPlayerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val context: Context = mockk()
    private val soundPool: SoundPool = mockk()
    private val errors = mutableListOf<Throwable>()
    private var listener: SoundPool.OnLoadCompleteListener? = null
    private lateinit var loadedFile: File

    @Test
    fun `公開コンストラクタは停止状態のプレイヤーを生成する`() {
        val player = AndroidSoundPlayer(RuntimeEnvironment.getApplication())

        assertFalse(player.isPlaying)
    }

    @Test
    fun `WAVの再生時間だけ再生し停止して一時ファイルを削除する`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                7
            }
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) } returns 11
            every { soundPool.stop(11) } returns Unit
            val bytes = wav()
            assertFalse(player.isPlaying)
            val job = launch { player.play(bytes, 50) }
            runCurrent()
            assertContentEquals(bytes, loadedFile.readBytes())
            assertFalse(player.isPlaying)
            listener!!.onLoadComplete(soundPool, 7, 0)
            runCurrent()
            assertTrue(player.isPlaying)
            advanceTimeBy(999)
            runCurrent()
            assertTrue(player.isPlaying)
            advanceTimeBy(1)
            runCurrent()
            job.join()
            assertTrue(errors.isEmpty())
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) }
            verify(exactly = 1) { soundPool.stop(11) }
            verifyOrder {
                soundPool.setOnLoadCompleteListener(match { it != null })
                soundPool.load(loadedFile.absolutePath, 1)
            }
            confirmVerified(soundPool)
        }

    @Test
    fun `別の音声IDの完了通知を無視して対象のロード失敗を報告する`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                7
            }
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.unload(7) } returns true
            val job = launch { player.play(wav(), 50) }
            runCurrent()
            listener!!.onLoadComplete(soundPool, 99, 0)
            runCurrent()
            assertFalse(job.isCompleted)
            listener!!.onLoadComplete(soundPool, 7, 3)
            job.join()
            assertEquals("SoundPool load failed: status=3", errors.single().message)
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.unload(7) }
            confirmVerified(soundPool)
        }

    @Test
    fun `通知が来なければ5秒でタイムアウトしてアンロードする`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                7
            }
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.unload(7) } returns true
            val job = launch { player.play(wav(), 50) }
            runCurrent()
            advanceTimeBy(4_999)
            runCurrent()
            assertFalse(job.isCompleted)
            advanceTimeBy(1)
            runCurrent()
            job.join()
            assertEquals("SoundPool load timed out: ${loadedFile.absolutePath}", errors.single().message)
            listener!!.onLoadComplete(soundPool, 7, 0)
            runCurrent()
            assertEquals(1, errors.size)
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.unload(7) }
            confirmVerified(soundPool)
        }

    @Test
    fun `再生開始に失敗すると音声をアンロードして報告する`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                7
            }
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) } returns 0
            every { soundPool.unload(7) } returns true
            val job = launch { player.play(wav(), 50) }
            runCurrent()
            listener!!.onLoadComplete(soundPool, 7, 0)
            job.join()
            assertEquals("SoundPool.play() failed", errors.single().message)
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) }
            verify(exactly = 1) { soundPool.unload(7) }
            confirmVerified(soundPool)
        }

    @Test
    fun `不正なWAVは報告して6秒再生する`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                7
            }
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) } returns 11
            every { soundPool.stop(11) } returns Unit
            val job = launch { player.play(byteArrayOf(1), 50) }
            runCurrent()
            listener!!.onLoadComplete(soundPool, 7, 0)
            runCurrent()
            advanceTimeBy(5_999)
            runCurrent()
            assertTrue(player.isPlaying)
            advanceTimeBy(1)
            runCurrent()
            job.join()
            assertEquals("WAV duration unknown: 1 bytes", errors.single().message)
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) }
            verify(exactly = 1) { soundPool.stop(11) }
            confirmVerified(soundPool)
        }

    @Test
    fun `再生中のキャンセルは停止して呼び出し元へ伝播する`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                7
            }
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) } returns 11
            every { soundPool.stop(11) } returns Unit
            val job = async { player.play(wav(), 50) }
            runCurrent()
            listener!!.onLoadComplete(soundPool, 7, 0)
            runCurrent()
            assertTrue(player.isPlaying)
            job.cancel()
            assertFailsWith<CancellationException> { job.await() }
            runCurrent()
            assertTrue(errors.isEmpty())
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.play(7, 0.5f, 0.5f, 1, 0, 1.0f) }
            verify(exactly = 1) { soundPool.stop(11) }
            confirmVerified(soundPool)
        }

    @Test
    fun `ロード中のキャンセルは音声をアンロードして遅れた失敗通知を無視する`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                7
            }
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.unload(7) } returns true
            val job = async { player.play(wav(), 50) }
            runCurrent()
            job.cancel()
            assertFailsWith<CancellationException> { job.await() }
            runCurrent()
            listener!!.onLoadComplete(soundPool, 7, 3)
            assertTrue(errors.isEmpty())
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.unload(7) }
            confirmVerified(soundPool)
        }

    @Test
    fun `ロードIDが0のままキャンセルされてもアンロードしない`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                0
            }
            val job = async { player.play(wav(), 50) }
            runCurrent()
            job.cancel()
            assertFailsWith<CancellationException> { job.await() }
            runCurrent()
            assertTrue(errors.isEmpty())
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            verify(exactly = 0) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 0) { soundPool.unload(0) }
            confirmVerified(soundPool)
        }

    @Test
    fun `ロード処理の例外は報告して一時ファイルを削除する`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)
            val failure = IllegalStateException("load exception")
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                loadedFile = File(firstArg<String>())
                throw failure
            }
            player.play(wav(), 50)
            assertEquals(failure.message, errors.single().message)
            assertTrue(errors.single() is IllegalStateException)
            assertFalse(player.isPlaying)
            assertFalse(loadedFile.exists())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 1) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 1) { soundPool.load(loadedFile.absolutePath, 1) }
            confirmVerified(soundPool)
        }

    @Test
    fun `音量を範囲内に制限して次の再生開始後に前の音声をアンロードする`() =
        runTest {
            val cacheDir = temporaryFolder.newFolder()
            every { context.cacheDir } returns cacheDir
            every { soundPool.setOnLoadCompleteListener(match { it != null }) } answers {
                listener = firstArg()
            }
            every { soundPool.setOnLoadCompleteListener(null) } returns Unit
            val paths = mutableListOf<String>()
            every { soundPool.load(match<String> { File(it).parentFile == cacheDir }, 1) } answers {
                paths.add(firstArg())
                if (paths.size == 1) 7 else 8
            }
            every { soundPool.play(7, 0f, 0f, 1, 0, 1.0f) } returns 11
            every { soundPool.play(8, 1f, 1f, 1, 0, 1.0f) } returns 12
            every { soundPool.stop(11) } returns Unit
            every { soundPool.stop(12) } returns Unit
            every { soundPool.unload(7) } returns true
            val player = AndroidSoundPlayer(context, soundPool, StandardTestDispatcher(testScheduler), errors::add)

            val first = launch { player.play(wav(), -1) }
            runCurrent()
            listener!!.onLoadComplete(soundPool, 7, 0)
            first.join()
            val second = launch { player.play(wav(), 101) }
            runCurrent()
            listener!!.onLoadComplete(soundPool, 8, 0)
            second.join()

            assertFalse(player.isPlaying)
            assertTrue(errors.isEmpty())
            assertTrue(cacheDir.listFiles().orEmpty().isEmpty())
            verify(exactly = 2) { soundPool.setOnLoadCompleteListener(match { it != null }) }
            verify(exactly = 2) { soundPool.setOnLoadCompleteListener(null) }
            verify(exactly = 1) { soundPool.load(paths[0], 1) }
            verify(exactly = 1) { soundPool.load(paths[1], 1) }
            verify(exactly = 1) { soundPool.play(7, 0f, 0f, 1, 0, 1.0f) }
            verify(exactly = 1) { soundPool.play(8, 1f, 1f, 1, 0, 1.0f) }
            verify(exactly = 1) { soundPool.stop(11) }
            verify(exactly = 1) { soundPool.stop(12) }
            verify(exactly = 1) { soundPool.unload(7) }
            verifyOrder {
                soundPool.play(8, 1f, 1f, 1, 0, 1.0f)
                soundPool.unload(7)
            }
            confirmVerified(soundPool)
        }

    private fun wav(): ByteArray =
        ByteBuffer
            .allocate(46)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("RIFF".toByteArray())
            .putInt(38)
            .put("WAVEfmt ".toByteArray())
            .putInt(16)
            .putShort(1)
            .putShort(1)
            .putInt(1)
            .putInt(2)
            .putShort(2)
            .putShort(16)
            .put("data".toByteArray())
            .putInt(2)
            .putShort(0)
            .array()
}
