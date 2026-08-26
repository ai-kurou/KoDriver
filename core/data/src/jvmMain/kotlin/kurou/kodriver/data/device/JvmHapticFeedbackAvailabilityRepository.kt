package kurou.kodriver.data.device

import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository

internal class JvmHapticFeedbackAvailabilityRepository : HapticFeedbackAvailabilityRepository {
    override fun isHapticFeedbackAvailable(): Boolean = false
}
