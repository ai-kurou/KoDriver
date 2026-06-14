package kurou.kodriver.feature.otherlicensedetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import kodriver.feature.otherlicensedetail.generated.resources.Res
import kodriver.feature.otherlicensedetail.generated.resources.license_title
import kodriver.feature.otherlicensedetail.generated.resources.navigate_back
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import org.jetbrains.compose.resources.stringResource

@Composable
fun OtherLicenseDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val libraries by produceLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
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
        )
    }
}
