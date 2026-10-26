package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechUnavailableReasonUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OtherListTtsAvailabilityFlowTest {
    private val repository: TextToSpeechRepository = mockk()

    @Test
    fun `初期状態は案内なし`() {
        val flow = createFlow()
        val items = listOf(OtherListItemType.HapticFeedback, OtherListItemType.KeepScreenOn)

        assertEquals(emptyList(), flow.uiState.value.guidanceItems)
        assertEquals(
            listOf(OtherListItemType.KeepScreenOn, OtherListItemType.HapticFeedback),
            flow.applyGuidance(items),
        )
        assertEquals(null, flow.uiState.value.ttsUnavailableGuidance)
        confirmVerified(repository)
    }

    @Test
    fun `判定結果を案内項目と公開案内へ変換する`() =
        runTest {
            coEvery { repository.unavailableReason() } returns TextToSpeechUnavailableReason.EngineMissing
            val flow = createFlow()

            flow.checkAvailability()

            assertEquals(
                OtherListTtsAvailabilityUiState(
                    guidanceItems = listOf(OtherListItemType.TtsEngineMissing),
                    ttsUnavailableGuidance = TtsUnavailableGuidance.EngineMissing,
                ),
                flow.uiState.value,
            )
            assertEquals(
                (buildOtherListItems() + OtherListItemType.TtsEngineMissing).sortedBy { it.ordinal },
                flow.applyGuidance(buildOtherListItems()),
            )
            coVerify(exactly = 1) { repository.unavailableReason() }
            confirmVerified(repository)
        }

    @Test
    fun `各利用不可理由を対応する項目と公開案内へ変換する`() =
        runTest {
            val cases =
                listOf(
                    TextToSpeechUnavailableReason.EngineMissing to
                        (OtherListItemType.TtsEngineMissing to TtsUnavailableGuidance.EngineMissing),
                    TextToSpeechUnavailableReason.LanguageDataMissing to
                        (OtherListItemType.TtsLanguageDataMissing to TtsUnavailableGuidance.LanguageDataMissing),
                    TextToSpeechUnavailableReason.WindowsSpeechUnavailable to
                        (OtherListItemType.WindowsSpeechUnavailable to TtsUnavailableGuidance.WindowsSpeechUnavailable),
                )
            val flow = createFlow()

            cases.forEach { (reason, expected) ->
                coEvery { repository.unavailableReason() } returns reason
                flow.checkAvailability()
                assertEquals(listOf(expected.first), flow.uiState.value.guidanceItems)
                assertEquals(
                    (buildOtherListItems() + expected.first).sortedBy { it.ordinal },
                    flow.applyGuidance(buildOtherListItems()),
                )
                assertEquals(expected.second, flow.uiState.value.ttsUnavailableGuidance)
            }

            coVerify(exactly = 3) { repository.unavailableReason() }
            confirmVerified(repository)
        }

    @Test
    fun `案内項目の差し替えでは他の項目を保持し古い案内を除去して順序を整える`() =
        runTest {
            coEvery { repository.unavailableReason() } returns TextToSpeechUnavailableReason.EngineMissing
            val flow = createFlow()
            flow.checkAvailability()

            assertEquals(
                listOf(
                    OtherListItemType.HapticFeedback,
                    OtherListItemType.TtsEngineMissing,
                    OtherListItemType.KeepScreenOn,
                ).sortedBy { it.ordinal },
                flow.applyGuidance(
                    listOf(
                        OtherListItemType.KeepScreenOn,
                        OtherListItemType.TtsLanguageDataMissing,
                        OtherListItemType.HapticFeedback,
                    ),
                ),
            )
            coVerify(exactly = 1) { repository.unavailableReason() }
            confirmVerified(repository)
        }

    @Test
    fun `利用可能になった場合は案内を消去する`() =
        runTest {
            coEvery { repository.unavailableReason() } returns null
            val flow = createFlow()

            flow.checkAvailability()

            assertEquals(emptyList(), flow.uiState.value.guidanceItems)
            assertEquals(buildOtherListItems(), flow.applyGuidance(buildOtherListItems()))
            assertEquals(null, flow.uiState.value.ttsUnavailableGuidance)
            coVerify(exactly = 1) { repository.unavailableReason() }
            confirmVerified(repository)
        }

    @Test
    fun `キャンセル例外は呼び出し元へ伝播し状態を更新しない`() =
        runTest {
            coEvery { repository.unavailableReason() } throws CancellationException()
            val flow = createFlow()

            assertFailsWith<CancellationException> { flow.checkAvailability() }

            assertEquals(emptyList(), flow.uiState.value.guidanceItems)
            assertEquals(buildOtherListItems(), flow.applyGuidance(buildOtherListItems()))
            assertEquals(null, flow.uiState.value.ttsUnavailableGuidance)
            coVerify(exactly = 1) { repository.unavailableReason() }
            confirmVerified(repository)
        }

    private fun createFlow() = OtherListTtsAvailabilityFlow(CheckTextToSpeechUnavailableReasonUseCase(repository))
}
