package kurou.kodriver.feature.narratoroverlay

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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

    /**
     * DataStore の初回読み出しのように、最初の値を emit しないまま待機し続ける Repository。
     */
    private class NeverEmittingOverlayVisiblePreferencesRepository : OverlayVisiblePreferencesRepository {
        override fun observeOverlayVisible(): Flow<Boolean> = flow { }

        override suspend fun saveOverlayVisible(visible: Boolean) = Unit
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `設定がONのときはオーバーレイを表示する`() =
        assertOverlayVisibleLabel(
            repository = FakeOverlayVisiblePreferencesRepository(visible = true),
            expectedLabel = "表示する",
        )

    @Test
    fun `設定がOFFのときはオーバーレイを表示しない`() =
        assertOverlayVisibleLabel(
            repository = FakeOverlayVisiblePreferencesRepository(visible = false),
            expectedLabel = "表示しない",
        )

    @Test
    fun `設定の読み込みが完了するまではnullを返す`() =
        assertOverlayVisibleLabel(
            repository = NeverEmittingOverlayVisiblePreferencesRepository(),
            expectedLabel = "読み込み中",
        )

    private fun assertOverlayVisibleLabel(
        repository: OverlayVisiblePreferencesRepository,
        expectedLabel: String,
    ) {
        startKoin {
            modules(module { factory { ObserveOverlayVisibleUseCase(repository) } })
        }

        runComposeUiTest {
            setContent {
                val visible = rememberNarratorOverlayVisible()
                Text(
                    when (visible) {
                        true -> "表示する"
                        false -> "表示しない"
                        null -> "読み込み中"
                    },
                )
            }

            onNodeWithText(expectedLabel).assertIsDisplayed()
        }
    }
}
