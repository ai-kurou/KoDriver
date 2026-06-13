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
import kodriver.app.shared.generated.resources.nav_more
import kurou.kodriver.feature.lmunarrator.fakeLmuNarratorDataModule
import kurou.kodriver.feature.readout.ReadoutContent
import kurou.kodriver.feature.readout.fakeReadoutDataModule
import org.jetbrains.compose.resources.stringResource
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin as koinStop

class AppScreenScreenshotTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpKoin() {
            startKoin { modules(listOf(fakeReadoutDataModule, fakeLmuNarratorDataModule) + appModules) }
        }

        @AfterClass
        @JvmStatic
        fun tearDownKoin() {
            koinStop()
        }
    }

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `NavigationDrawer 読み上げタブ`() {
        composeRule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationDrawer,
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
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationDrawer,
                    readoutContent = {
                        ReadoutContent(scaffoldDirective = twoPaneDirective)
                    },
                )
            }
        }
        composeRule.onNodeWithText(navMore).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationRail 読み上げタブ`() {
        composeRule.setContent {
            Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
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
            Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    readoutContent = {
                        ReadoutContent(scaffoldDirective = singlePaneDirective)
                    },
                )
            }
        }
        composeRule.onNodeWithText(navMore).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationBar 読み上げタブ`() {
        composeRule.setContent {
            Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    readoutContent = {
                        ReadoutContent(scaffoldDirective = singlePaneDirective)
                    },
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `NavigationBar その他タブ`() {
        var navMore by mutableStateOf("")

        composeRule.setContent {
            navMore = stringResource(Res.string.nav_more)
            Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    readoutContent = {
                        ReadoutContent(scaffoldDirective = singlePaneDirective)
                    },
                )
            }
        }
        composeRule.onNodeWithText(navMore).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }
}
