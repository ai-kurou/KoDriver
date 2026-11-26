package kurou.kodriver.domain.manualreadout.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.manualreadout.model.ManualReadoutItemRequest

/** 項目単位の依頼が実際に読み上げられたことを、アプリ内で配信する。音声タブが、読み上げられた語句を強調するために購読する。 */
public interface ManualReadoutItemSpokenRepository {
    public fun observeSpoken(): Flow<ManualReadoutItemRequest>

    public suspend fun notifySpoken(request: ManualReadoutItemRequest)
}
