package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * バーチャルエナジー残量アナウンス詳細設定の ViewModel。
 * 現時点では空の状態を公開するのみ。設定項目を追加する際に UseCase を注入して uiState を組み立てる。
 */
internal class LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutRemainingVirtualEnergyDetailUiState> =
        MutableStateFlow(LmuWindowsReadoutRemainingVirtualEnergyDetailUiState).asStateFlow()
}
