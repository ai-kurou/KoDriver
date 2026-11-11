package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutItemRequest

/** 項目単位の依頼が実際に読み上げられたことを、アプリ内で配信する。音声タブが、読み上げられた語句を強調するために購読する。 */
interface ManualReadoutItemSpokenRepository {
    fun observeSpoken(): Flow<ManualReadoutItemRequest>

    suspend fun notifySpoken(request: ManualReadoutItemRequest)
}
