package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import java.io.File

/**
 * 保存先ディレクトリ・ファイル名・[Serializer] を渡すだけで [DataStore] を組み立てる汎用ファクトリ。
 */
internal fun <T> preferencesDataStore(
    directory: String,
    fileName: String,
    serializer: Serializer<T>,
): DataStore<T> =
    FallbackOnReadErrorDataStore(
        delegate =
            DataStoreFactory.create(
                serializer = serializer,
                produceFile = { File("$directory/$fileName") },
                corruptionHandler = ReplaceFileCorruptionHandler { serializer.defaultValue },
            ),
        defaultValue = serializer.defaultValue,
    )
