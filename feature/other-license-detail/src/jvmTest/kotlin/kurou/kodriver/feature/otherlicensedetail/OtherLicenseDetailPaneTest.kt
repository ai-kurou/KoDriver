package kurou.kodriver.feature.otherlicensedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.Scm
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class OtherLicenseDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @MockK
    lateinit var uriHandler: UriHandler

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

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
