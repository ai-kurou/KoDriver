package kurou.kodriver.domain.manualreadout.repository

public interface ManualReadoutSettingsResetter {
    public suspend fun resetToDefault()
}
