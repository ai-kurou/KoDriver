package kurou.kodriver.feature.otheroverlaytextcolordetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import org.koin.dsl.module

/** 実DataStoreへの書き込みを避けるテスト用モジュール。 */
val fakeOtherOverlayTextColorDetailModule =
    module {
        single<OverlayTextColorPreferencesRepository> { FakeOverlayTextColorPreferencesRepository() }
    }

class FakeOverlayTextColorPreferencesRepository : OverlayTextColorPreferencesRepository {
    private val flow = MutableStateFlow(OVERLAY_TEXT_COLOR_DEFAULT)

    private val preview = MutableStateFlow<Long?>(null)

    override fun observeOverlayTextColor(): Flow<Long> = combine(flow, preview) { saved, preview -> preview ?: saved }

    override fun setPreviewOverlayTextColor(argb: Long?) {
        preview.update { argb }
    }

    override suspend fun saveOverlayTextColor(argb: Long) {
        flow.update { argb }
    }
}
