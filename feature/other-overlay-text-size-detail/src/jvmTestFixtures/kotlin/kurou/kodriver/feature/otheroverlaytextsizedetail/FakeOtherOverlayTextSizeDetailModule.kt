package kurou.kodriver.feature.otheroverlaytextsizedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:data の代わりに OverlayTextSizePreferencesRepository の
 * Fake 実装をバインドし、実DataStore（`~/.kodriver`）への書き込みを避ける。
 */
val fakeOtherOverlayTextSizeDetailModule =
    module {
        single<OverlayTextSizePreferencesRepository> { FakeOverlayTextSizePreferencesRepository() }
    }

class FakeOverlayTextSizePreferencesRepository : OverlayTextSizePreferencesRepository {
    private val flow = MutableStateFlow(OverlayTextSize.MEDIUM)

    private val preview = MutableStateFlow<OverlayTextSize?>(null)

    override fun observeOverlayTextSize(): Flow<OverlayTextSize> =
        combine(flow, preview) { saved, preview -> preview ?: saved }

    override fun setPreviewOverlayTextSize(overlayTextSize: OverlayTextSize?) {
        preview.update { overlayTextSize }
    }

    override suspend fun saveOverlayTextSize(overlayTextSize: OverlayTextSize) {
        flow.update { overlayTextSize }
    }
}
