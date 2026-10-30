package kurou.kodriver.domain.repository

interface ManualReadoutSettingsResetter {
    suspend fun resetToDefault()
}
