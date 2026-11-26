package kurou.kodriver.domain.autoreadout.repository

public interface AutoReadoutSettingsResetter {
    public suspend fun resetToDefault()
}
