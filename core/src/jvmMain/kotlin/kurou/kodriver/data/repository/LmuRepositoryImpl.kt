package kurou.kodriver.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kurou.kodriver.data.datasource.SharedMemoryReader
import kurou.kodriver.data.mapper.LmuMapper
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.repository.LmuRepository

class LmuRepositoryImpl(
    private val pollingIntervalMs: Long = 16L,
    private val reconnectIntervalMs: Long = 1_000L,
) : LmuRepository {

    private val reader = SharedMemoryReader(
        segmentName = "LMU_Data",
        // LMUObjectOut サイズ: generic(332) + paths(1300) + scoring(126832) + telemetry(196356) = 324820
        sizeBytes = 324_820,
    )

    override fun telemetryStream(): Flow<LmuTelemetryData> = flow {
        try {
            while (true) {
                if (!reader.isOpen()) {
                    if (!reader.open()) {
                        delay(reconnectIntervalMs)
                        continue
                    }
                }
                reader.readBuffer()?.let { emit(LmuMapper.map(it)) }
                delay(pollingIntervalMs)
            }
        } finally {
            reader.close()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        if (reader.isOpen()) return@withContext true
        val opened = reader.open()
        if (!opened) reader.close()
        opened
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        reader.close()
    }
}
