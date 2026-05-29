package kurou.kodriver

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform