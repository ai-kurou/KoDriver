package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.banner_simulator_disconnected
import kodriver.app.shared.generated.resources.nav_log
import kodriver.app.shared.generated.resources.nav_more
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.readoutlist.ReadoutContent
import kurou.kodriver.feature.readoutlist.fakeReadoutListModule
import kurou.kodriver.feature.readoutlist.readoutListModule
import kurou.kodriver.feature.telemetryloglist.TelemetryLogContent
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogListModule
import kurou.kodriver.feature.telemetryloglist.telemetryLogListModule
import org.jetbrains.compose.resources.stringResource
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin as koinStop

class AppScreenScreenshotTest {

    companion object {
        @OptIn(ExperimentalCoroutinesApi::class)
        private val testDispatcher = UnconfinedTestDispatcher()

        @OptIn(ExperimentalCoroutinesApi::class)
        @BeforeClass
        @JvmStatic
        fun setUpKoin() {
            Dispatchers.setMain(testDispatcher)
            startKoin {
                modules(
                    listOf(
                        fakeReadoutListModule,
                        fakeTelemetryLogListModule,
                        readoutListModule,
                        telemetryLogListModule,
                    ),
                )
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @AfterClass
        @JvmStatic
        fun tearDownKoin() {
            koinStop()
            Dispatchers.resetMain()
        }
    }

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `NavigationDrawer 読み上げタブ`() {
        composeRule.setContent {
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationDrawer,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    readoutContent = {
                        ReadoutContent(scaffoldDirective = twoPaneDirective)
                    },
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationDrawer その他タブ`() {
        var navMore by mutableStateOf("")

        composeRule.setContent {
            navMore = stringResource(Res.string.nav_more)
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationDrawer,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    otherContent = {
                        OtherContent(
                            uiState = OtherListUiState(),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = twoPaneDirective,
                        )
                    },
                )
            }
        }
        composeRule.onNodeWithText(navMore).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationDrawer ログタブ`() {
        var navLog by mutableStateOf("")

        composeRule.setContent {
            navLog = stringResource(Res.string.nav_log)
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationDrawer,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    telemetryLogContent = { TelemetryLogContent() },
                )
            }
        }
        composeRule.onNodeWithText(navLog).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationRail 読み上げタブ`() {
        composeRule.setContent {
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    readoutContent = {
                        ReadoutContent(scaffoldDirective = singlePaneDirective)
                    },
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationRail その他タブ`() {
        var navMore by mutableStateOf("")

        composeRule.setContent {
            navMore = stringResource(Res.string.nav_more)
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    otherContent = {
                        OtherContent(
                            uiState = OtherListUiState(),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = singlePaneDirective,
                        )
                    },
                )
            }
        }
        composeRule.onNodeWithText(navMore).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationRail ログタブ`() {
        var navLog by mutableStateOf("")

        composeRule.setContent {
            navLog = stringResource(Res.string.nav_log)
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    telemetryLogContent = { TelemetryLogContent() },
                )
            }
        }
        composeRule.onNodeWithText(navLog).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationBar 読み上げタブ`() {
        composeRule.setContent {
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    readoutContent = {
                        ReadoutContent(scaffoldDirective = singlePaneDirective)
                    },
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationBar ログタブ`() {
        var navLog by mutableStateOf("")

        composeRule.setContent {
            navLog = stringResource(Res.string.nav_log)
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    telemetryLogContent = { TelemetryLogContent() },
                )
            }
        }
        composeRule.onNodeWithText(navLog).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationBar その他タブ`() {
        var navMore by mutableStateOf("")

        composeRule.setContent {
            navMore = stringResource(Res.string.nav_more)
            val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
            Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    bannerUiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = bannerMessage,
                    ),
                    hasAppUpdate = true,
                    otherContent = {
                        OtherContent(
                            uiState = OtherListUiState(),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = singlePaneDirective,
                        )
                    },
                )
            }
        }
        composeRule.onNodeWithText(navMore).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }
}
