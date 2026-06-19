package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.SoundVolumeRepository
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPane
import kurou.kodriver.feature.othervolumedetail.otherVolumeDetailModule
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentScreenshotTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    companion object {
        private val testDispatcher = UnconfinedTestDispatcher()

        @BeforeClass
        @JvmStatic
        fun setUpKoin() {
            Dispatchers.setMain(testDispatcher)
            startKoin {
                modules(
                    otherVolumeDetailModule,
                    module {
                        single<SoundVolumeRepository> { FakeSoundVolumeRepository(initialVolume = 80) }
                    },
                )
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDownKoin() {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ライセンス詳細を表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                        OtherContent(
                            uiState = OtherListUiState(selectedItem = OtherListItemType.License),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = twoPaneDirective,
                            detailContent = { _, _, _ ->
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    androidx.compose.material3.Text("License Detail")
                                }
                            },
                        )
                    }
                }
            }
        }

        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `音量詳細を表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                        OtherContent(
                            uiState = OtherListUiState(selectedItem = OtherListItemType.Volume),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = twoPaneDirective,
                            detailContent = { itemType, canNavigateBack, onBack ->
                                if (itemType == OtherListItemType.Volume) {
                                    OtherVolumeDetailPane(canNavigateBack, onBack)
                                }
                            },
                        )
                    }
                }
            }
        }

        rule.onRoot().captureRoboImage()
    }
}

private class FakeSoundVolumeRepository(initialVolume: Int) : SoundVolumeRepository {
    private val volume = MutableStateFlow(initialVolume)

    override fun volume(): Flow<Int> = volume

    override suspend fun saveVolume(volume: Int) {
        this.volume.update { volume }
    }
}
