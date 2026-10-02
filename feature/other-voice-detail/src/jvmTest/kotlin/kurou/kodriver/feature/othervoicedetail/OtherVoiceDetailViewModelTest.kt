@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.othervoicedetail

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherVoiceDetailViewModelTest {
    @Test
    fun `初期状態は空の表示状態を返す`() =
        runTest {
            val viewModel = OtherVoiceDetailViewModel()

            assertEquals(OtherVoiceDetailUiState, viewModel.uiState.first())
        }
}
