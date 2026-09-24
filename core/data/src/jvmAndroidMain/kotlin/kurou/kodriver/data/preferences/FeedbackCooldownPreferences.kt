package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class FeedbackCooldownPreferences(
    @ProtoNumber(1) val lastSentAtEpochMillis: Long = 0L,
)
