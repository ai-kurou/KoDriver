package kurou.kodriver.feature.otherlicensedetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import kodriver.feature.otherlicensedetail.generated.resources.Res
import kodriver.feature.otherlicensedetail.generated.resources.license_title
import kodriver.feature.otherlicensedetail.generated.resources.navigate_back
import kurou.kodriver.core.designsystem.DetailPaneHeader
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            DetailPaneHeader(
                title = stringResource(Res.string.license_title),
                canNavigateBack = canNavigateBack,
                navigateBackContentDescription = stringResource(Res.string.navigate_back),
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        )
    }
}
