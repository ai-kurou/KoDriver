package kurou.kodriver.domain.repository

public interface AutoReadoutSettingsResetter {
    public suspend fun resetToDefault()
}
