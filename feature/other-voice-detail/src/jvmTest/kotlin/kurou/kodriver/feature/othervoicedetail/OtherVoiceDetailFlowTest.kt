package kurou.kodriver.feature.othervoicedetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherVoiceDetailFlowTest {
    private val voice = TextToSpeechVoice("voice-a", "音声A", "ja-JP")
    private val loadState = MutableStateFlow(OtherVoiceDetailUiState())
    private val savedVoiceId = MutableStateFlow(VOICE_ID_UNSPECIFIED)
    private val previewingVoiceId = MutableStateFlow<String?>(null)

    private fun createFlow() =
        OtherVoiceDetailFlow(
            loadState = loadState,
            savedVoiceId = savedVoiceId,
            previewingVoiceId = previewingVoiceId,
        )

    @Test
    fun `ロード状態と保存済み音声の有無から不在を判定し保存済みIDを保持する`() =
        runTest {
            val flow = createFlow()
            for (loading in listOf(true, false)) {
                for (voices in listOf(emptyList(), listOf(voice))) {
                    for (id in listOf(VOICE_ID_UNSPECIFIED, voice.id, "missing")) {
                        loadState.update { OtherVoiceDetailUiState(voices = voices, isLoading = loading) }
                        savedVoiceId.update { id }
                        assertEquals(
                            OtherVoiceDetailUiState(
                                voices = voices,
                                selectedVoiceId = id,
                                isLoading = loading,
                                savedVoiceMissing =
                                    !loading && id != VOICE_ID_UNSPECIFIED && (id == "missing" || voices.isEmpty()),
                            ),
                            flow.uiState.first(),
                        )
                    }
                }
            }
        }

    @Test
    fun `再読み込みのロード状態と選択IDと試聴中IDの更新を反映する`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherVoiceDetailUiState(), flow.uiState.first())

            savedVoiceId.update { voice.id }
            previewingVoiceId.update { "missing" }
            loadState.update { OtherVoiceDetailUiState(isLoading = false) }
            assertEquals(
                OtherVoiceDetailUiState(
                    selectedVoiceId = voice.id,
                    isLoading = false,
                    savedVoiceMissing = true,
                    previewingVoiceId = "missing",
                ),
                flow.uiState.first(),
            )

            loadState.update { OtherVoiceDetailUiState() }
            assertEquals(
                OtherVoiceDetailUiState(selectedVoiceId = voice.id, previewingVoiceId = "missing"),
                flow.uiState.first(),
            )

            loadState.update { OtherVoiceDetailUiState(voices = listOf(voice), isLoading = false) }
            previewingVoiceId.update { VOICE_ID_UNSPECIFIED }
            assertEquals(
                OtherVoiceDetailUiState(
                    voices = listOf(voice),
                    selectedVoiceId = voice.id,
                    isLoading = false,
                    previewingVoiceId = VOICE_ID_UNSPECIFIED,
                ),
                flow.uiState.first(),
            )

            savedVoiceId.update { VOICE_ID_UNSPECIFIED }
            previewingVoiceId.update { null }
            assertEquals(
                OtherVoiceDetailUiState(voices = listOf(voice), isLoading = false),
                flow.uiState.first(),
            )
        }
}
