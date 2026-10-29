package kurou.kodriver.feature.othervoicedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoiceDetailFlowTest {
    private val voice = TextToSpeechVoice("voice-a", "音声A", "ja-JP")
    private val refreshTrigger = MutableStateFlow(0)
    private val loadVoices: suspend () -> List<TextToSpeechVoice> = mockk()
    private val savedVoiceId = MutableStateFlow(VOICE_ID_UNSPECIFIED)
    private val previewingVoiceId = MutableStateFlow<String?>(null)

    private fun createFlow() =
        OtherVoiceDetailFlow(
            refreshTrigger = refreshTrigger,
            loadVoices = loadVoices,
            savedVoiceId = savedVoiceId,
            previewingVoiceId = previewingVoiceId,
        )

    @Test
    fun `初回購読は取得中を通知してから音声一覧と保存済み選択を反映する`() =
        runTest {
            val result = CompletableDeferred<List<TextToSpeechVoice>>()
            coEvery { loadVoices() } coAnswers { result.await() }
            savedVoiceId.update { voice.id }
            val states = mutableListOf<OtherVoiceDetailUiState>()
            val job = createFlow().uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            assertEquals(listOf(OtherVoiceDetailUiState(selectedVoiceId = voice.id)), states)

            result.complete(listOf(voice))
            runCurrent()

            assertEquals(
                listOf(
                    OtherVoiceDetailUiState(selectedVoiceId = voice.id),
                    OtherVoiceDetailUiState(voices = listOf(voice), selectedVoiceId = voice.id, isLoading = false),
                ),
                states,
            )
            job.cancel()
            coVerify(exactly = 1) { loadVoices() }
            confirmVerified(loadVoices)
        }

    @Test
    fun `再読み込みは取得中を再通知して音声一覧を再取得する`() =
        runTest {
            val initialResult = CompletableDeferred<List<TextToSpeechVoice>>()
            val retryResult = CompletableDeferred<List<TextToSpeechVoice>>()
            coEvery { loadVoices() } coAnswers { initialResult.await() }
            savedVoiceId.update { voice.id }
            previewingVoiceId.update { "missing" }
            val states = mutableListOf<OtherVoiceDetailUiState>()
            val job = createFlow().uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            val loading = OtherVoiceDetailUiState(selectedVoiceId = voice.id, previewingVoiceId = "missing")
            assertEquals(listOf(loading), states)
            initialResult.complete(emptyList())
            runCurrent()
            val missing = loading.copy(isLoading = false, savedVoiceMissing = true)
            assertEquals(listOf(loading, missing), states)

            coEvery { loadVoices() } coAnswers { retryResult.await() }
            refreshTrigger.update { it + 1 }
            runCurrent()
            assertEquals(listOf(loading, missing, loading), states)
            retryResult.complete(listOf(voice))
            runCurrent()
            assertEquals(
                listOf(loading, missing, loading, loading.copy(voices = listOf(voice), isLoading = false)),
                states,
            )
            job.cancel()
            coVerify(exactly = 2) { loadVoices() }
            confirmVerified(loadVoices)
        }

    @Test
    fun `購読解除後に同じFlowを再購読すると取得中から再実行する`() =
        runTest {
            val initialResult = CompletableDeferred<List<TextToSpeechVoice>>()
            val nextResult = CompletableDeferred<List<TextToSpeechVoice>>()
            coEvery { loadVoices() } coAnswers { initialResult.await() }
            val flow = createFlow()
            val states = mutableListOf<OtherVoiceDetailUiState>()
            val firstJob = flow.uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            initialResult.complete(listOf(voice))
            runCurrent()
            firstJob.cancel()
            firstJob.join()

            coEvery { loadVoices() } coAnswers { nextResult.await() }
            val secondJob = flow.uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            assertEquals(
                listOf(
                    OtherVoiceDetailUiState(),
                    OtherVoiceDetailUiState(voices = listOf(voice), isLoading = false),
                    OtherVoiceDetailUiState(),
                ),
                states,
            )
            nextResult.complete(emptyList())
            runCurrent()
            assertEquals(OtherVoiceDetailUiState(isLoading = false), states.last())
            assertEquals(4, states.size)
            secondJob.cancel()
            coVerify(exactly = 2) { loadVoices() }
            confirmVerified(loadVoices)
        }

    @Test
    fun `ロード完了後も保存済み音声の有無と試聴中IDの更新を反映する`() =
        runTest {
            val result = CompletableDeferred<List<TextToSpeechVoice>>()
            coEvery { loadVoices() } coAnswers { result.await() }
            val states = mutableListOf<OtherVoiceDetailUiState>()
            val job = createFlow().uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            result.complete(listOf(voice))
            runCurrent()

            for (id in listOf(VOICE_ID_UNSPECIFIED, voice.id, "missing")) {
                savedVoiceId.update { id }
                previewingVoiceId.update { id }
                runCurrent()
                assertEquals(
                    OtherVoiceDetailUiState(
                        voices = listOf(voice),
                        selectedVoiceId = id,
                        isLoading = false,
                        savedVoiceMissing = id == "missing",
                        previewingVoiceId = id,
                    ),
                    states.last(),
                )
            }
            previewingVoiceId.update { null }
            runCurrent()
            assertEquals(null, states.last().previewingVoiceId)
            job.cancel()
            coVerify(exactly = 1) { loadVoices() }
            confirmVerified(loadVoices)
        }
}
