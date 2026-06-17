package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AppUpdateRepository

class CheckAppUpdateAvailableUseCase(
    private val repository: AppUpdateRepository,
) {
    suspend operator fun invoke(currentVersion: String): Boolean {
        val latest = repository.getLatestRelease() ?: return false
        val latestParts = parseVersion(latest.tagName.trimStart('v'))
        val currentParts = parseVersion(currentVersion.trimStart('v'))
        return latestParts.compareTo(currentParts) > 0
    }

    private fun parseVersion(version: String): List<Int> {
        val parts = version.split(".").map { it.toIntOrNull() ?: 0 }
        return List(3) { parts.getOrElse(it) { 0 } }
    }

    private fun List<Int>.compareTo(other: List<Int>): Int {
        for (i in indices) {
            val diff = this[i] - other[i]
            if (diff != 0) return diff
        }
        return 0
    }
}
