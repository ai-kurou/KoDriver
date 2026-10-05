package kurou.kodriver.domain.preview

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutSpeechEventPreviewHelperTest {
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val playSpeechEvent: PlaySpeechEventUseCase = mockk()
    private val event = SpeechEvent.TyreWearWarning(50, "残り50%")

    private fun TestScope.helper() =
        ReadoutSpeechEventPreviewHelper(backgroundScope, checkAvailable, observeVolume, playSpeechEvent)

    @Test
    fun `利用可否は購読なしで一度取得し空白と利用不可では音量を取得しない`() =
        runTest {
            coEvery { checkAvailable() } returns false
            val unavailable = helper()
            assertFalse(unavailable.textToSpeechAvailable.value)
            unavailable.preview("残り50%", event)
            runCurrent()
            unavailable.preview("残り50%", event)
            coEvery { checkAvailable() } returns true
            val available = helper()
            runCurrent()
            assertTrue(available.textToSpeechAvailable.value)
            available.preview("", event)
            available.preview(" \t\n", event)
            coVerify(exactly = 2) { checkAvailable() }
            verify(exactly = 0) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(event) }
            confirmVerified(checkAvailable, observeVolume, playSpeechEvent)
        }

    @Test
    fun `現在の音量が正数の場合だけ受け取ったイベントをそのまま再生する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returnsMany listOf(flowOf(0), flowOf(-1), flowOf(1), flowOf(42))
            every { playSpeechEvent(event) } returns Unit
            val helper = helper()
            runCurrent()
            repeat(4) { helper.preview("残り50%", event) }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 4) { observeVolume() }
            verify(exactly = 2) { playSpeechEvent(event) }
            confirmVerified(checkAvailable, observeVolume, playSpeechEvent)
        }

    @Test
    fun `音量取得中に呼び出し元をキャンセルするとイベントを再生しない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            val requested = CompletableDeferred<Unit>()
            val volume = CompletableDeferred<Int>()
            every { observeVolume() } returns
                flow {
                    requested.complete(Unit)
                    emit(volume.await())
                }
            val helper = helper()
            runCurrent()
            val job = launch { helper.preview("残り50%", event) }
            requested.await()
            job.cancel()
            job.join()
            volume.complete(42)
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 1) { observeVolume() }
            verify(exactly = 0) { playSpeechEvent(event) }
            confirmVerified(checkAvailable, observeVolume, playSpeechEvent)
        }

    @Test
    fun `イベント再生の失敗は呼び出し元に伝播する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            val failure = IllegalStateException("failed")
            every { playSpeechEvent(event) } throws failure
            val helper = helper()
            runCurrent()
            assertEquals(failure, assertFailsWith<IllegalStateException> { helper.preview("残り50%", event) })
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 1) { observeVolume() }
            verify(exactly = 1) { playSpeechEvent(event) }
            confirmVerified(checkAvailable, observeVolume, playSpeechEvent)
        }
}
