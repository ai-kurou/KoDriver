package kurou.kodriver.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.gt7Ps5ReadoutRemainingFuelDetailModule
import kurou.kodriver.feature.readoutlist.ReadoutListItemType
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutItemDetailContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        startKoin {
            modules(
                module {
                    single<Gt7Ps5RemainingFuelPreferencesRepository> {
                        FakeGt7Ps5RemainingFuelPreferencesRepository()
                    }
                },
                gt7Ps5ReadoutRemainingFuelDetailModule,
            )
        }
    }

    @After
    fun tearDown() {
        rule.setContent {}
        rule.waitForIdle()
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `GT7燃料残量の読み上げ項目は燃料残量詳細ペインを表示する`() {
        rule.setContent {
            ReadoutItemDetailContent(ReadoutListItemType.Gt7Ps5.RemainingFuel)
        }

        rule.onNodeWithText("燃料残量警告").assertIsDisplayed()
        rule.onNodeWithText("燃料残量が設定した閾値を下回った場合に、音声でお知らせします。")
            .assertIsDisplayed()
        rule.onNodeWithText("30%").assertIsDisplayed()
    }
}

private class FakeGt7Ps5RemainingFuelPreferencesRepository : Gt7Ps5RemainingFuelPreferencesRepository {
    private val threshold = MutableStateFlow(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

    override fun observeThresholdPercentage(): Flow<Int> = threshold

    override suspend fun saveThresholdPercentage(percentage: Int) {
        threshold.value = percentage
    }
}
