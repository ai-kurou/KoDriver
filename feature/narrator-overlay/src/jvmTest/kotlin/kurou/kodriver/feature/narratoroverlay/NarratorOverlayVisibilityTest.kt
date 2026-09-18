package kurou.kodriver.feature.narratoroverlay

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class NarratorOverlayVisibilityTest {
    private class FakeOverlayVisiblePreferencesRepository(
        visible: Boolean,
    ) : OverlayVisiblePreferencesRepository {
        private val state = MutableStateFlow(visible)

        override fun observeOverlayVisible(): Flow<Boolean> = state

        override suspend fun saveOverlayVisible(visible: Boolean) {
            state.value = visible
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `設定がONのときはオーバーレイを表示する`() = assertOverlayVisible(visible = true, expectedText = "表示する")

    @Test
    fun `設定がOFFのときはオーバーレイを表示しない`() = assertOverlayVisible(visible = false, expectedText = "表示しない")

    private fun assertOverlayVisible(
        visible: Boolean,
        expectedText: String,
    ) {
        startKoin {
            modules(
                module {
                    factory { ObserveOverlayVisibleUseCase(FakeOverlayVisiblePreferencesRepository(visible)) }
                },
            )
        }

        runComposeUiTest {
            setContent {
                Text(if (rememberNarratorOverlayVisible()) "表示する" else "表示しない")
            }

            onNodeWithText(expectedText).assertIsDisplayed()
        }
    }
}
