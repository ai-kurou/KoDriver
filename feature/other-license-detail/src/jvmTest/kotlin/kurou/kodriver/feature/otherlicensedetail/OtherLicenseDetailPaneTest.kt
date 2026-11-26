package kurou.kodriver.feature.otherlicensedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.Scm
import com.mikepenz.aboutlibraries.util.parseData
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kurou.kodriver.feature.otherlicensedetail.generated.resources.Res
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherLicenseDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    val uriHandler: UriHandler = mockk()

    private val haptic: HapticFeedback = mockk(relaxUnitFun = true)

    @Test
    fun `ライセンス一覧を表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                OtherLicenseDetailPane(
                    canNavigateBack = true,
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNodeWithText("ライセンス").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `サイトのあるライブラリの行をタップすると触覚の後にサイトを開く`() {
        every { uriHandler.openUri("https://example.com/website") } answers {
            verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        }
        setPaneContent(testLibrary(website = "https://example.com/website", scmUrl = null))

        rule.onNodeWithText("Test Library").performClick()
        rule.waitForIdle()

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { uriHandler.openUri("https://example.com/website") }
        confirmVerified(haptic, uriHandler)
    }

    @Test
    fun `サイトのないライブラリの行をタップしても触覚が発生する`() {
        setPaneContent(testLibrary(website = null, scmUrl = null))

        rule.onNodeWithText("Test Library").performClick()
        rule.waitForIdle()

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic, uriHandler)
    }

    private fun setPaneContent(library: Library) {
        rule.setContent {
            CompositionLocalProvider(
                LocalHapticFeedback provides haptic,
                LocalUriHandler provides uriHandler,
            ) {
                MaterialTheme {
                    OtherLicenseDetailPaneContent(
                        libraries = Libs(libraries = listOf(library), licenses = emptySet()),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }
        }
    }

    @Test
    fun `同梱のVoskと日本語モデルがライセンス一覧のデータに含まれる`() {
        runBlocking {
            val json = Res.readBytes("files/aboutlibraries.json").decodeToString()
            val libraries: Map<String, Library> = parseData(json).libraries.associateBy { it.uniqueId }

            val vosk = libraries["com.alphacephei:vosk"]
            val voskModel = libraries["com.alphacephei:vosk-model-small-ja"]
            assertEquals("Vosk", vosk?.name)
            assertEquals("vosk-model-small-ja-0.22", voskModel?.name)
            assertEquals(listOf("Apache-2.0"), vosk?.licenses?.map { it.spdxId })
            assertEquals(listOf("Apache-2.0"), voskModel?.licenses?.map { it.spdxId })
        }
    }

    @Test
    fun `websiteが設定されている場合はwebsiteを開いてtrueを返す`() {
        every { uriHandler.openUri(any()) } returns Unit
        val library =
            testLibrary(
                website = "https://example.com/website",
                scmUrl = "https://example.com/scm",
            )

        val opened = openLibraryWebsite(library, uriHandler)

        assertEquals(true, opened)
        verify(exactly = 1) { uriHandler.openUri("https://example.com/website") }
        confirmVerified(uriHandler)
    }

    @Test
    fun `websiteが未設定の場合はscmのURLを開いてtrueを返す`() {
        every { uriHandler.openUri(any()) } returns Unit
        val library =
            testLibrary(
                website = null,
                scmUrl = "https://example.com/scm",
            )

        val opened = openLibraryWebsite(library, uriHandler)

        assertEquals(true, opened)
        verify(exactly = 1) { uriHandler.openUri("https://example.com/scm") }
        confirmVerified(uriHandler)
    }

    @Test
    fun `websiteもscmのURLも未設定の場合は何も開かずfalseを返す`() {
        val library = testLibrary(website = null, scmUrl = null)

        val opened = openLibraryWebsite(library, uriHandler)

        assertEquals(false, opened)
        verify(exactly = 0) { uriHandler.openUri(any()) }
        confirmVerified(uriHandler)
    }

    private fun testLibrary(
        website: String?,
        scmUrl: String?,
    ) = Library(
        uniqueId = "test:library",
        artifactVersion = "1.0.0",
        name = "Test Library",
        description = null,
        website = website,
        developers = emptyList(),
        organization = null,
        scm = scmUrl?.let { Scm(connection = null, developerConnection = null, url = it) },
        licenses = emptySet(),
        funding = emptySet(),
    )
}
