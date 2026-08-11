package kurou.kodriver.feature.otherlicensedetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.feature.otherlicensedetail.generated.resources.Res
import kurou.kodriver.feature.otherlicensedetail.generated.resources.license_title
import kurou.kodriver.feature.otherlicensedetail.generated.resources.navigate_back
import org.jetbrains.compose.resources.stringResource

/**
 * OtherLicenseDetail の画面を表示する Composable。
 */
@Composable
fun OtherLicenseDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val libraries by produceLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    val uriHandler = LocalUriHandler.current
    DetailPaneScaffold(
        title = stringResource(Res.string.license_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.fillMaxSize(),
            // true を返すと aboutlibraries 標準のライセンス詳細ダイアログ表示を抑制し、website/scm を直接開く独自挙動のみにする。
            onLibraryClick = { library ->
                openLibraryWebsite(library, uriHandler)
                true
            },
        )
    }
}

/**
 * ライブラリの公式サイト（無ければリポジトリURL）を外部ブラウザで開く。
 */
internal fun openLibraryWebsite(
    library: Library,
    uriHandler: UriHandler,
) {
    val url = library.website?.takeIf { it.isNotBlank() } ?: library.scm?.url?.takeIf { it.isNotBlank() }
    url?.let(uriHandler::openUri)
}
