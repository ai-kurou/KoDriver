package kurou.kodriver.domain.repository

public interface ManualReadoutSettingsResetter {
    public suspend fun resetToDefault()
}
