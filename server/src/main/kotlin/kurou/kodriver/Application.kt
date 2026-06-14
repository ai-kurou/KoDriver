package kurou.kodriver

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    KoDriverServer().start(wait = true)
}

class KoDriverServer(
    port: Int = DEFAULT_PORT,
    host: String = DEFAULT_HOST,
) {
    private val server = embeddedServer(
        factory = Netty,
        port = port,
        host = host,
        module = Application::module,
    )

    fun start(wait: Boolean = false) {
        server.start(wait = wait)
    }

    fun stop() {
        server.stop()
    }

    private companion object {
        const val DEFAULT_PORT = 8080
        const val DEFAULT_HOST = "0.0.0.0"
    }
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello, Ktor!")
        }
    }
}
