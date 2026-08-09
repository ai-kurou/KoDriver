@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.feature.otherthemedetail.FakeThemePreferencesRepository
import kurou.kodriver.feature.otherthemedetail.fakeOtherThemeDetailModule
import kurou.kodriver.feature.otherthemedetail.otherThemeDetailModule
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "night")
class AppThemeModeAndroidTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val composeRule = createComposeRule()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        startKoin {
            modules(
                listOf(
                    fakeOtherThemeDetailModule,
                    otherThemeDetailModule,
                ),
            )
        }
    }

    @AfterTest
    fun tearDown() {
        GlobalContext.stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `LIGHT設定ではシステムのダーク状態に関係なくfalseを返す`() =
        runTest {
            repository.saveThemeMode(ThemeMode.LIGHT)

            assertFalse(captureAppDarkTheme())
        }

    @Test
    fun `DARK設定ではシステムのダーク状態に関係なくtrueを返す`() =
        runTest {
            repository.saveThemeMode(ThemeMode.DARK)

            assertTrue(captureAppDarkTheme())
        }

    @Test
    fun `SYSTEM設定ではAndroidのシステムダーク状態を使う`() =
        runTest {
            repository.saveThemeMode(ThemeMode.SYSTEM)

            assertTrue(captureAppDarkTheme())
        }

    @Test
    @Config(sdk = [36], qualifiers = "notnight")
    fun `SYSTEM設定ではAndroidのシステムライト状態を使う`() =
        runTest {
            repository.saveThemeMode(ThemeMode.SYSTEM)

            assertFalse(captureAppDarkTheme())
        }

    private val repository: FakeThemePreferencesRepository
        get() = GlobalContext.get().get<ThemePreferencesRepository>() as FakeThemePreferencesRepository

    private fun captureAppDarkTheme(): Boolean {
        var darkTheme: Boolean? = null
        composeRule.setContent {
            darkTheme = rememberAppDarkTheme()
        }
        composeRule.waitForIdle()
        return darkTheme ?: error("App dark theme was not captured.")
    }
}
