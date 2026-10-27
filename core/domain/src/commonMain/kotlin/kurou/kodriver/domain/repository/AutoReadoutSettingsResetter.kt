package kurou.kodriver.domain.repository

interface AutoReadoutSettingsResetter {
    suspend fun resetToDefault()
}
