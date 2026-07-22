package kurou.kodriver

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsNearbyVehicleData
import kurou.kodriver.domain.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsNearbyVehiclesRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsNearbyVehiclesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.io.IOException
import java.net.ServerSocket
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {

    @Test
    fun `バージョンエンドポイントはアプリバージョンをJSONで返す`() = testApplication {
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }
        val response = client.get("/version")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"version":"${BuildConfig.APP_VERSION}"}""", response.bodyAsText())
    }

    @Test
    fun `ルートはサーバーの応答を返す`() = testApplication {
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
    }

    @Test
    fun `フラッグ情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsFlagRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(repository),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/flags") {
            repository.emit(greenFlagData)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(
                """{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE","sectorFlags":["CLEAR","YELLOW","CLEAR"],""" +
                    """"startLight":4,"numRedLights":2,"playerFlag":"BLUE","playerUnderYellow":true,""" +
                    """"playerCountLapFlag":"COUNT_LAP_AND_TIME"}""",
                message,
            )
        }
    }

    @Test
    fun `フラッグ情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsFlagRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(repository),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/flags") {
            repository.emit(greenFlagData)
            repository.emit(greenFlagData)
            repository.emit(yellowFlagData)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(greenFlagJson, first)
            assertEquals(yellowFlagJson, second)
        }
    }

    @Test
    fun `フラッグWebSocketはクライアント切断時に送信Flowをキャンセルする`() = testApplication {
        val repository = CancellableLmuWindowsFlagRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(repository),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/flags") {
            close()
        }

        withTimeout(1_000) {
            repository.cancelled.await()
        }
    }

    @Test
    fun `近接情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsVehicleApproachRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(repository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/vehicle_approach") {
            repository.emit(vehicleApproachDataLeft)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(
                """{"sideBySideLeftVehicleIds":[3],"sideBySideRightVehicleIds":[],""" +
                    """"lateralDistanceLeftMeters":1.5,"lateralDistanceRightMeters":1.7976931348623157E308}""",
                message,
            )
        }
    }

    @Test
    fun `近接情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsVehicleApproachRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(repository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/vehicle_approach") {
            repository.emit(vehicleApproachDataLeft)
            repository.emit(vehicleApproachDataLeft)
            repository.emit(vehicleApproachDataRight)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(vehicleApproachLeftJson, first)
            assertEquals(vehicleApproachRightJson, second)
        }
    }

    @Test
    fun `車両故障情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsVehicleDamageRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(repository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/damage") {
            repository.emit(overheatingDamage)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(
                """{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.5}""",
                message,
            )
        }
    }

    @Test
    fun `車両故障情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsVehicleDamageRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(repository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/damage") {
            repository.emit(overheatingDamage)
            repository.emit(overheatingDamage)
            repository.emit(partDetachedDamage)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(overheatingDamageJson, first)
            assertEquals(partDetachedDamageJson, second)
        }
    }

    @Test
    fun `カーカス温度情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsTyreCarcassTemperatureRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repository),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/tyre_carcass_temperature") {
            repository.emit(tyreCarcassTemperatureData1)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(tyreCarcassTemperatureJson1, message)
        }
    }

    @Test
    fun `カーカス温度情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsTyreCarcassTemperatureRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repository),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/tyre_carcass_temperature") {
            repository.emit(tyreCarcassTemperatureData1)
            repository.emit(tyreCarcassTemperatureData1)
            repository.emit(tyreCarcassTemperatureData2)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(tyreCarcassTemperatureJson1, first)
            assertEquals(tyreCarcassTemperatureJson2, second)
        }
    }

    @Test
    fun `バーチャルエナジー残量をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsVirtualEnergyRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(repository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/virtual_energy") {
            repository.emit(virtualEnergyData1)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(virtualEnergyJson1, message)
        }
    }

    @Test
    fun `バーチャルエナジー残量の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsVirtualEnergyRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(repository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/virtual_energy") {
            repository.emit(virtualEnergyData1)
            repository.emit(virtualEnergyData1)
            repository.emit(virtualEnergyData2)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(virtualEnergyJson1, first)
            assertEquals(virtualEnergyJson2, second)
        }
    }

    @Test
    fun `近くの車両情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsNearbyVehiclesRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(repository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/nearby_vehicles") {
            repository.emit(nearbyVehiclesData1)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(nearbyVehiclesJson1, message)
        }
    }

    @Test
    fun `近くの車両情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsNearbyVehiclesRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(repository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/nearby_vehicles") {
            repository.emit(nearbyVehiclesData1)
            repository.emit(nearbyVehiclesData1)
            repository.emit(nearbyVehiclesData2)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(nearbyVehiclesJson1, first)
            assertEquals(nearbyVehiclesJson2, second)
        }
    }

    @Test
    fun `KoDriverServerはstartで起動しstopで停止する`() {
        val port = ServerSocket(0).use { it.localPort }
        val server = KoDriverServer(
            useCases = KoDriverServerUseCases(
                observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
            ),
            port = port,
            host = "127.0.0.1",
        )
        server.serviceAdvertiser = KoDriverServiceAdvertiser(jmdnsFactory = { throw IOException("test") })
        server.start()
        try {
            val response = URI("http://127.0.0.1:$port/").toURL().readText()
            assertEquals("Hello, Ktor!", response)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `タイミング情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(repository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/my_best_lap") {
            repository.emit(timingTelemetryData1)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(timingJson1, message)
        }
    }

    @Test
    fun `タイミング情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsRepository()
        application {
            module(
                KoDriverServerUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
                    observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyTyreCarcassTemperatureRepository,
                    ),
                    observeLmuWindows = ObserveLmuWindowsUseCase(repository),
                    observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
                    observeNearbyVehicles = ObserveLmuWindowsNearbyVehiclesUseCase(EmptyNearbyVehiclesRepository),
                ),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/my_best_lap") {
            repository.emit(timingTelemetryData1)
            repository.emit(timingTelemetryData1)
            repository.emit(timingTelemetryData2)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(timingJson1, first)
            assertEquals(timingJson2, second)
        }
    }

    @Test
    fun `createKoDriverServerはKoinから依存を解決してKoDriverServerを生成する`() {
        val koin = startKoin {
            modules(
                module {
                    single<LmuWindowsFlagRepository> { FakeLmuWindowsFlagRepository() }
                    single<LmuWindowsVehicleApproachRepository> { EmptyVehicleApproachRepository }
                    single<LmuWindowsVehicleDamageRepository> { EmptyVehicleDamageRepository }
                    single<LmuWindowsTyreCarcassTemperatureRepository> { EmptyTyreCarcassTemperatureRepository }
                    single<LmuWindowsRepository> { EmptyLmuWindowsRepository }
                    single<LmuWindowsVirtualEnergyRepository> { EmptyVirtualEnergyRepository }
                    single<LmuWindowsNearbyVehiclesRepository> { EmptyNearbyVehiclesRepository }
                },
            )
        }.koin
        try {
            val server = createKoDriverServer(koin)
            assertNotNull(server)
        } finally {
            stopKoin()
        }
    }
}

// --- テストデータ ---

private val greenFlagData = LmuWindowsRaceFlagsData(
    gamePhase = SessionPhase.GREEN_FLAG,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
    startLight = 4,
    numRedLights = 2,
    playerFlag = PrimaryFlag.BLUE,
    playerUnderYellow = true,
    playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
)

private val yellowFlagData = LmuWindowsRaceFlagsData(
    gamePhase = SessionPhase.FULL_COURSE_YELLOW,
    yellowFlagState = SessionYellowFlagState.PENDING,
    sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.YELLOW, SectorFlagState.YELLOW),
    startLight = 0,
    numRedLights = 0,
    playerFlag = PrimaryFlag.UNKNOWN,
    playerUnderYellow = true,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)

private const val greenFlagJson =
    """{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE","sectorFlags":["CLEAR","YELLOW","CLEAR"],""" +
        """"startLight":4,"numRedLights":2,"playerFlag":"BLUE","playerUnderYellow":true,""" +
        """"playerCountLapFlag":"COUNT_LAP_AND_TIME"}"""

private const val yellowFlagJson =
    """{"gamePhase":"FULL_COURSE_YELLOW","yellowFlagState":"PENDING","sectorFlags":["YELLOW","YELLOW","YELLOW"],""" +
        """"startLight":0,"numRedLights":0,"playerFlag":"UNKNOWN","playerUnderYellow":true,""" +
        """"playerCountLapFlag":"DO_NOT_COUNT_LAP_OR_TIME"}"""

private val vehicleApproachDataLeft = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = setOf(3),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = 1.5,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private val vehicleApproachDataRight = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = setOf(5),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = 2.0,
)

private const val vehicleApproachLeftJson =
    """{"sideBySideLeftVehicleIds":[3],"sideBySideRightVehicleIds":[],""" +
        """"lateralDistanceLeftMeters":1.5,"lateralDistanceRightMeters":1.7976931348623157E308}"""

private const val vehicleApproachRightJson =
    """{"sideBySideLeftVehicleIds":[],"sideBySideRightVehicleIds":[5],""" +
        """"lateralDistanceLeftMeters":1.7976931348623157E308,"lateralDistanceRightMeters":2.0}"""

private val overheatingDamage = LmuWindowsVehicleDamageData(
    overheating = true,
    partDetached = false,
    lastImpactMagnitude = 0.5,
)

private val partDetachedDamage = LmuWindowsVehicleDamageData(
    overheating = false,
    partDetached = true,
    lastImpactMagnitude = 1.2,
)

private const val overheatingDamageJson =
    """{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.5}"""

private const val partDetachedDamageJson =
    """{"overheating":false,"partDetached":true,"lastImpactMagnitude":1.2}"""

private val tyreCarcassTemperatureData1 = LmuWindowsTyreCarcassTemperatureData(
    wheels = mapOf(
        WheelIndex.FRONT_LEFT to 80.0,
        WheelIndex.FRONT_RIGHT to 82.0,
        WheelIndex.REAR_LEFT to 85.0,
        WheelIndex.REAR_RIGHT to 87.0,
    ),
)

private val tyreCarcassTemperatureData2 = LmuWindowsTyreCarcassTemperatureData(
    wheels = mapOf(
        WheelIndex.FRONT_LEFT to 90.0,
        WheelIndex.FRONT_RIGHT to 91.0,
        WheelIndex.REAR_LEFT to 92.0,
        WheelIndex.REAR_RIGHT to 93.0,
    ),
)

private const val tyreCarcassTemperatureJson1 =
    """{"wheels":{"FRONT_LEFT":80.0,"FRONT_RIGHT":82.0,"REAR_LEFT":85.0,"REAR_RIGHT":87.0}}"""

private const val tyreCarcassTemperatureJson2 =
    """{"wheels":{"FRONT_LEFT":90.0,"FRONT_RIGHT":91.0,"REAR_LEFT":92.0,"REAR_RIGHT":93.0}}"""

// --- Fake リポジトリ ---

private class FakeLmuWindowsFlagRepository : LmuWindowsFlagRepository {
    private val channel = Channel<LmuWindowsRaceFlagsData>(capacity = Channel.UNLIMITED)

    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsRaceFlagsData) {
        channel.trySend(data).getOrThrow()
    }
}

private class CancellableLmuWindowsFlagRepository : LmuWindowsFlagRepository {
    val cancelled = CompletableDeferred<Unit>()

    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = flow {
        try {
            awaitCancellation()
        } finally {
            cancelled.complete(Unit)
        }
    }
}

private class FakeLmuWindowsVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    private val channel = Channel<LmuWindowsVehicleApproachData>(capacity = Channel.UNLIMITED)

    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsVehicleApproachData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
}

private object EmptyVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private class FakeLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    private val channel = Channel<LmuWindowsVehicleDamageData>(capacity = Channel.UNLIMITED)

    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsVehicleDamageData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private class FakeLmuWindowsTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    private val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(capacity = Channel.UNLIMITED)

    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsTyreCarcassTemperatureData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

private class FakeLmuWindowsRepository : LmuWindowsRepository {
    private val channel = Channel<LmuWindowsTelemetryData>(capacity = Channel.UNLIMITED)

    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = channel.receiveAsFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit

    fun emit(data: LmuWindowsTelemetryData) {
        channel.trySend(data).getOrThrow()
    }
}

private val emptyWheels = WheelIndex.entries.associateWith { LmuWindowsTyreWheelData(0.0, 0.0, 0.0, 0.0, 0.0) }

private val timingTelemetryData1 = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = LmuWindowsTyreData(wheels = emptyWheels),
    fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = LmuWindowsTimingData(
        currentLapTimeMs = 65_000L,
        lastLapTimeMs = 90_000L,
        bestLapTimeMs = 88_000L,
        sector1Ms = 30_000L,
        sector1And2Ms = 28_000L,
        currentLap = 3,
        maxLaps = 10,
    ),
    vehicle = LmuWindowsVehicleData(
        localVelocityX = 0.0, localVelocityY = 0.0, localVelocityZ = 0.0,
        positionX = 0.0, positionY = 0.0, positionZ = 0.0,
    ),
)

private val timingTelemetryData2 = timingTelemetryData1.copy(
    timing = LmuWindowsTimingData(
        currentLapTimeMs = 70_000L,
        lastLapTimeMs = 91_000L,
        bestLapTimeMs = 88_000L,
        sector1Ms = 31_000L,
        sector1And2Ms = 29_000L,
        currentLap = 4,
        maxLaps = 10,
    ),
)

private const val timingJson1 =
    """{"currentLapTimeMs":65000,"lastLapTimeMs":90000,"bestLapTimeMs":88000,""" +
        """"sector1Ms":30000,"sector1And2Ms":28000,"currentLap":3,"maxLaps":10}"""

private const val timingJson2 =
    """{"currentLapTimeMs":70000,"lastLapTimeMs":91000,"bestLapTimeMs":88000,""" +
        """"sector1Ms":31000,"sector1And2Ms":29000,"currentLap":4,"maxLaps":10}"""

private val virtualEnergyData1 = LmuWindowsVirtualEnergyData(remainingRatio = 0.5, session = 10)
private val virtualEnergyData2 = LmuWindowsVirtualEnergyData(remainingRatio = 0.3, session = 10)

private const val virtualEnergyJson1 = """{"remainingRatio":0.5,"session":10}"""
private const val virtualEnergyJson2 = """{"remainingRatio":0.3,"session":10}"""

private object EmptyVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = emptyFlow()
}

private object EmptyNearbyVehiclesRepository : LmuWindowsNearbyVehiclesRepository {
    override fun nearbyVehiclesStream(): Flow<LmuWindowsNearbyVehiclesData> = emptyFlow()
}

private val nearbyVehiclesData1 = LmuWindowsNearbyVehiclesData(
    vehicles = listOf(
        LmuWindowsNearbyVehicleData(vehicleId = 1, longitudinalDistanceMeters = 3.0, lateralDistanceMeters = -2.0),
    ),
)
private val nearbyVehiclesData2 = LmuWindowsNearbyVehiclesData(vehicles = emptyList())

private const val nearbyVehiclesJson1 =
    """{"vehicles":[{"vehicleId":1,"longitudinalDistanceMeters":3.0,"lateralDistanceMeters":-2.0}]}"""
private const val nearbyVehiclesJson2 = """{"vehicles":[]}"""

private class FakeLmuWindowsNearbyVehiclesRepository : LmuWindowsNearbyVehiclesRepository {
    private val channel = Channel<LmuWindowsNearbyVehiclesData>(capacity = Channel.UNLIMITED)

    override fun nearbyVehiclesStream(): Flow<LmuWindowsNearbyVehiclesData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsNearbyVehiclesData) {
        channel.trySend(data).getOrThrow()
    }
}

private class FakeLmuWindowsVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    private val channel = Channel<LmuWindowsVirtualEnergyData>(capacity = Channel.UNLIMITED)

    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsVirtualEnergyData) {
        channel.trySend(data).getOrThrow()
    }
}
