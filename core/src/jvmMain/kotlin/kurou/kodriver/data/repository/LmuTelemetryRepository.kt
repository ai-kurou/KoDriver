package kurou.kodriver.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kurou.kodriver.data.datasource.Rf2SharedMemoryReader
import kurou.kodriver.data.mapper.LmuTelemetryMapper
import kurou.kodriver.domain.model.TelemetryData
import kurou.kodriver.domain.repository.TelemetryRepository

class LmuTelemetryRepository(
    private val pollingIntervalMs: Long = 16L,
    private val reconnectIntervalMs: Long = 1_000L,
) : TelemetryRepository {

    private val reader = Rf2SharedMemoryReader(
        segmentName = "LMU_Data",
        sizeBytes = 524_288,
    )

    override fun telemetryStream(): Flow<TelemetryData> = flow {
        try {
            while (true) {
                if (!reader.isOpen()) {
                    if (!reader.open()) {
                        delay(reconnectIntervalMs)
                        continue
                    }
                }
                reader.readBuffer()?.let { emit(LmuTelemetryMapper.map(it)) }
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
