package kurou.kodriver.domain.preview

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutTextPreviewHelperTest {
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val key = ReadoutItemKey.Gt7Ps5.MyBestLap.Root

    private fun TestScope.helper() =
        ReadoutTextPreviewHelper(backgroundScope, checkAvailable, observeVolume, playStartSound, speakText)

    @Test
    fun `利用可否は初期値falseから購読なしで一度取得する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            val helper = helper()
            assertFalse(helper.textToSpeechAvailable.value)
            helper.preview("試聴", key)
            runCurrent()
            assertTrue(helper.textToSpeechAvailable.value)
            runCurrent()
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 0) { observeVolume() }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `空白文言と利用不可では音量も再生も取得しない`() =
        runTest {
            coEvery { checkAvailable() } returns false
            val unavailable = helper()
            runCurrent()
            unavailable.preview("試聴", key)
            coEvery { checkAvailable() } returns true
            val available = helper()
            runCurrent()
            available.preview("", key)
            available.preview("  ", key)
            coVerify(exactly = 2) { checkAvailable() }
            verify(exactly = 0) { observeVolume() }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロと負数では開始音も本文も再生しない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returnsMany listOf(flowOf(0), flowOf(-1))
            val helper = helper()
            runCurrent()
            repeat(2) { helper.preview("試聴", key) }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `項目の開始音完了後に本文を現在の音量で再生する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returnsMany listOf(flowOf(1), flowOf(42))
            val completed = CompletableDeferred<Unit>()
            coEvery { playStartSound(key) } coAnswers { completed.await() }
            coEvery { speakText("試聴", volume = 1) } returns Unit
            coEvery { speakText("次の試聴", volume = 42) } returns Unit
            val helper = helper()
            runCurrent()
            val job = launch { helper.preview("試聴", key) }
            runCurrent()
            coVerify(exactly = 0) { speakText("試聴", volume = 1) }
            completed.complete(Unit)
            job.join()
            helper.preview("次の試聴", key)
            coVerifyOrder {
                playStartSound(key)
                speakText("試聴", volume = 1)
                playStartSound(key)
                speakText("次の試聴", volume = 42)
            }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 2) { playStartSound(key) }
            coVerify(exactly = 1) { speakText("試聴", volume = 1) }
            coVerify(exactly = 1) { speakText("次の試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `開始音のキャンセルと失敗は呼び出し元に伝播し本文を再生しない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            val helper = helper()
            runCurrent()
            val cancellation = CancellationException("cancelled")
            coEvery { playStartSound(key) } throws cancellation
            assertEquals(cancellation, assertFailsWith<CancellationException> { helper.preview("試聴", key) })
            val failure = IllegalStateException("failed")
            coEvery { playStartSound(key) } throws failure
            assertEquals(failure, assertFailsWith<IllegalStateException> { helper.preview("試聴", key) })
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 2) { playStartSound(key) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }
}
