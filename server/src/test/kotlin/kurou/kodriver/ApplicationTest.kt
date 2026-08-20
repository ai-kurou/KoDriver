package kurou.kodriver

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
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
    fun `バージョンエンドポイントはアプリバージョンをJSONで返す`() =
        testApplication {
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }
            val response = client.get("/version")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("""{"version":"${BuildConfig.APP_VERSION}"}""", response.bodyAsText())
        }

    @Test
    fun `ルートはサーバーの応答を返す`() =
        testApplication {
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("""{"status":"ok"}""", response.bodyAsText())
        }

    @Test
    fun `フラッグ情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsFlagRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(repository),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/flags") {
                    repository.emit(greenFlagData)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(
                        """{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE",""" +
                            """"sectorFlags":["CLEAR","YELLOW","CLEAR"],""" +
                            """"startLight":4,"numRedLights":2,"playerFlag":"BLUE","playerUnderYellow":true,""" +
                            """"playerCountLapFlag":"COUNT_LAP_AND_TIME"}""",
                        message,
                    )
                }
        }

    @Test
    fun `フラッグ情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsFlagRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(repository),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/flags") {
                    repository.emit(greenFlagData)
                    repository.emit(greenFlagData)
                    repository.emit(yellowFlagData)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(GREEN_FLAG_JSON, first)
                    assertEquals(YELLOW_FLAG_JSON, second)
                }
        }

    @Test
    fun `Originヘッダを持つWebSocket接続は拒否される`() =
        testApplication {
            val repository = FakeLmuWindowsFlagRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(repository),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket(
                    "/ws/lmu_windows/flags",
                    request = { header(HttpHeaders.Origin, "https://evil.example.com") },
                ) {
                    val reason = withTimeout(1_000) { closeReason.await() }
                    assertEquals(CloseReason.Codes.VIOLATED_POLICY.code, reason?.code)
                }
        }

    @Test
    fun `近接情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsVehicleApproachRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(repository),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/vehicle_approach") {
                    repository.emit(vehicleApproachDataLeft)

                    val message =
                        withTimeout(1_000) {
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
    fun `近接情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsVehicleApproachRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(repository),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/vehicle_approach") {
                    repository.emit(vehicleApproachDataLeft)
                    repository.emit(vehicleApproachDataLeft)
                    repository.emit(vehicleApproachDataRight)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(VEHICLE_APPROACH_LEFT_JSON, first)
                    assertEquals(VEHICLE_APPROACH_RIGHT_JSON, second)
                }
        }

    @Test
    fun `車両故障情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsVehicleDamageRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(repository),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/damage") {
                    repository.emit(overheatingDamage)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(
                        """{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.5}""",
                        message,
                    )
                }
        }

    @Test
    fun `車両故障情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsVehicleDamageRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(repository),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/damage") {
                    repository.emit(overheatingDamage)
                    repository.emit(overheatingDamage)
                    repository.emit(partDetachedDamage)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(OVERHEATING_DAMAGE_JSON, first)
                    assertEquals(PART_DETACHED_DAMAGE_JSON, second)
                }
        }

    @Test
    fun `カーカス温度情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsTyreCarcassTemperatureRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                repository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/tyre_carcass_temperature") {
                    repository.emit(tyreCarcassTemperatureData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(TYRE_CARCASS_TEMPERATURE_JSON_1, message)
                }
        }

    @Test
    fun `カーカス温度情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsTyreCarcassTemperatureRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                repository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/tyre_carcass_temperature") {
                    repository.emit(tyreCarcassTemperatureData1)
                    repository.emit(tyreCarcassTemperatureData1)
                    repository.emit(tyreCarcassTemperatureData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(TYRE_CARCASS_TEMPERATURE_JSON_1, first)
                    assertEquals(TYRE_CARCASS_TEMPERATURE_JSON_2, second)
                }
        }

    @Test
    fun `車両クラス情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsVehicleClassRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                repository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/vehicle_class") {
                    repository.emit(vehicleClassData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(VEHICLE_CLASS_JSON_1, message)
                }
        }

    @Test
    fun `車両クラス情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsVehicleClassRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                repository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/vehicle_class") {
                    repository.emit(vehicleClassData1)
                    repository.emit(vehicleClassData1)
                    repository.emit(vehicleClassData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(VEHICLE_CLASS_JSON_1, first)
                    assertEquals(VEHICLE_CLASS_JSON_2, second)
                }
        }

    @Test
    fun `摩耗情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsTyreWearRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(repository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/tyre_wear") {
                    repository.emit(tyreWearData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(TYRE_WEAR_JSON_1, message)
                }
        }

    @Test
    fun `摩耗情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsTyreWearRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(repository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/tyre_wear") {
                    repository.emit(tyreWearData1)
                    repository.emit(tyreWearData1)
                    repository.emit(tyreWearData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(TYRE_WEAR_JSON_1, first)
                    assertEquals(TYRE_WEAR_JSON_2, second)
                }
        }

    @Test
    fun `バーチャルエナジー残量をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsVirtualEnergyRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(repository),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/virtual_energy") {
                    repository.emit(virtualEnergyData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(VIRTUAL_ENERGY_JSON_1, message)
                }
        }

    @Test
    fun `バーチャルエナジー残量の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsVirtualEnergyRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(repository),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/virtual_energy") {
                    repository.emit(virtualEnergyData1)
                    repository.emit(virtualEnergyData1)
                    repository.emit(virtualEnergyData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(VIRTUAL_ENERGY_JSON_1, first)
                    assertEquals(VIRTUAL_ENERGY_JSON_2, second)
                }
        }

    @Test
    fun `ACE燃料残量をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeAceWindowsFuelRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(repository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/ace_windows/fuel") {
                    repository.emit(aceFuelData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(ACE_FUEL_JSON_1, message)
                }
        }

    @Test
    fun `ACE燃料残量の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeAceWindowsFuelRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(repository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/ace_windows/fuel") {
                    repository.emit(aceFuelData1)
                    repository.emit(aceFuelData1)
                    repository.emit(aceFuelData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(ACE_FUEL_JSON_1, first)
                    assertEquals(ACE_FUEL_JSON_2, second)
                }
        }

    @Test
    fun `ACEフラッグ情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeAceWindowsFlagRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(repository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/ace_windows/flags") {
                    repository.emit(aceFlagData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(ACE_FLAG_JSON_1, message)
                }
        }

    @Test
    fun `ACEフラッグ情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeAceWindowsFlagRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(repository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/ace_windows/flags") {
                    repository.emit(aceFlagData1)
                    repository.emit(aceFlagData1)
                    repository.emit(aceFlagData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(ACE_FLAG_JSON_1, first)
                    assertEquals(ACE_FLAG_JSON_2, second)
                }
        }

    @Test
    fun `ACEステータスをJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeAceWindowsStatusRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(repository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/ace_windows/status") {
                    repository.emit(aceStatusData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(ACE_STATUS_JSON_1, message)
                }
        }

    @Test
    fun `ACEステータスの同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeAceWindowsStatusRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(repository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/ace_windows/status") {
                    repository.emit(aceStatusData1)
                    repository.emit(aceStatusData1)
                    repository.emit(aceStatusData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(ACE_STATUS_JSON_1, first)
                    assertEquals(ACE_STATUS_JSON_2, second)
                }
        }

    @Test
    fun `ピット状態をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsPitStatusRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus = ObserveLmuWindowsPitStatusUseCase(repository),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/pit_status") {
                    repository.emit(pitStatusData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(PIT_STATUS_JSON_1, message)
                }
        }

    @Test
    fun `ピット状態の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsPitStatusRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus = ObserveLmuWindowsPitStatusUseCase(repository),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/pit_status") {
                    repository.emit(pitStatusData1)
                    repository.emit(pitStatusData1)
                    repository.emit(pitStatusData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(PIT_STATUS_JSON_1, first)
                    assertEquals(PIT_STATUS_JSON_2, second)
                }
        }

    @Test
    fun `KoDriverServerはstartで起動しstopで停止する`() {
        val port = ServerSocket(0).use { it.localPort }
        val server =
            KoDriverServer(
                useCases =
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                port = port,
                host = "127.0.0.1",
            )
        server.serviceAdvertiser = KoDriverServiceAdvertiser(jmdnsFactory = { throw IOException("test") })
        server.start()
        try {
            val response = URI("http://127.0.0.1:$port/").toURL().readText()
            assertEquals("""{"status":"ok"}""", response)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `タイミング情報をJSONでWebSocketへ送信する`() =
        testApplication {
            val repository = FakeLmuWindowsRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(repository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/my_best_lap") {
                    repository.emit(timingTelemetryData1)

                    val message =
                        withTimeout(1_000) {
                            (incoming.receive() as Frame.Text).readText()
                        }
                    assertEquals(TIMING_JSON_1, message)
                }
        }

    @Test
    fun `タイミング情報の同一値は重複して送信されない`() =
        testApplication {
            val repository = FakeLmuWindowsRepository()
            application {
                module(
                    KoDriverServerUseCases(
                        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(FakeLmuWindowsFlagRepository()),
                        observeLmuWindowsVehicleApproach =
                            ObserveLmuWindowsVehicleApproachUseCase(
                                EmptyLmuWindowsVehicleApproachRepository,
                            ),
                        observeLmuWindowsVehicleDamage =
                            ObserveLmuWindowsVehicleDamageUseCase(
                                EmptyLmuWindowsVehicleDamageRepository,
                            ),
                        observeLmuWindowsTyreCarcassTemperature =
                            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                                EmptyLmuWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsVehicleClass =
                            ObserveLmuWindowsVehicleClassUseCase(
                                EmptyLmuWindowsVehicleClassRepository,
                            ),
                        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                        observeLmuWindows = ObserveLmuWindowsUseCase(repository),
                        observeLmuWindowsVirtualEnergy =
                            ObserveLmuWindowsVirtualEnergyUseCase(
                                EmptyLmuWindowsVirtualEnergyRepository,
                            ),
                        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
                        observeAceWindowsTyreCarcassTemperature =
                            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                                EmptyAceWindowsTyreCarcassTemperatureRepository,
                            ),
                        observeLmuWindowsPitStatus =
                            ObserveLmuWindowsPitStatusUseCase(
                                EmptyLmuWindowsPitStatusRepository,
                            ),
                    ),
                )
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/my_best_lap") {
                    repository.emit(timingTelemetryData1)
                    repository.emit(timingTelemetryData1)
                    repository.emit(timingTelemetryData2)

                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals(TIMING_JSON_1, first)
                    assertEquals(TIMING_JSON_2, second)
                }
        }

    @Test
    fun `createKoDriverServerはKoinから依存を解決してKoDriverServerを生成する`() {
        val koin =
            startKoin {
                modules(
                    module {
                        single<LmuWindowsFlagRepository> { FakeLmuWindowsFlagRepository() }
                        single<LmuWindowsVehicleApproachRepository> { EmptyLmuWindowsVehicleApproachRepository }
                        single<LmuWindowsVehicleDamageRepository> { EmptyLmuWindowsVehicleDamageRepository }
                        single<LmuWindowsTyreCarcassTemperatureRepository> {
                            EmptyLmuWindowsTyreCarcassTemperatureRepository
                        }
                        single<LmuWindowsVehicleClassRepository> { EmptyLmuWindowsVehicleClassRepository }
                        single<LmuWindowsTyreWearRepository> { EmptyLmuWindowsTyreWearRepository }
                        single<LmuWindowsRepository> { EmptyLmuWindowsRepository }
                        single<LmuWindowsVirtualEnergyRepository> { EmptyLmuWindowsVirtualEnergyRepository }
                        single<AceWindowsFuelRepository> { EmptyAceWindowsFuelRepository }
                        single<AceWindowsFlagRepository> { EmptyAceWindowsFlagRepository }
                        single<AceWindowsStatusRepository> { EmptyAceWindowsStatusRepository }
                        single<AceWindowsTyreCarcassTemperatureRepository> {
                            EmptyAceWindowsTyreCarcassTemperatureRepository
                        }
                        single<LmuWindowsPitStatusRepository> { EmptyLmuWindowsPitStatusRepository }
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

private val greenFlagData =
    LmuWindowsRaceFlagsData(
        gamePhase = SessionPhase.GREEN_FLAG,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
        startLight = 4,
        numRedLights = 2,
        playerFlag = PrimaryFlag.BLUE,
        playerUnderYellow = true,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )

private val yellowFlagData =
    LmuWindowsRaceFlagsData(
        gamePhase = SessionPhase.FULL_COURSE_YELLOW,
        yellowFlagState = SessionYellowFlagState.PENDING,
        sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.YELLOW, SectorFlagState.YELLOW),
        startLight = 0,
        numRedLights = 0,
        playerFlag = PrimaryFlag.UNKNOWN,
        playerUnderYellow = true,
        playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
    )

private const val GREEN_FLAG_JSON =
    """{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE","sectorFlags":["CLEAR","YELLOW","CLEAR"],""" +
        """"startLight":4,"numRedLights":2,"playerFlag":"BLUE","playerUnderYellow":true,""" +
        """"playerCountLapFlag":"COUNT_LAP_AND_TIME"}"""

private const val YELLOW_FLAG_JSON =
    """{"gamePhase":"FULL_COURSE_YELLOW","yellowFlagState":"PENDING","sectorFlags":["YELLOW","YELLOW","YELLOW"],""" +
        """"startLight":0,"numRedLights":0,"playerFlag":"UNKNOWN","playerUnderYellow":true,""" +
        """"playerCountLapFlag":"DO_NOT_COUNT_LAP_OR_TIME"}"""

private val vehicleApproachDataLeft =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(3),
        sideBySideRightVehicleIds = emptySet(),
        lateralDistanceLeftMeters = 1.5,
        lateralDistanceRightMeters = Double.MAX_VALUE,
    )

private val vehicleApproachDataRight =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = emptySet(),
        sideBySideRightVehicleIds = setOf(5),
        lateralDistanceLeftMeters = Double.MAX_VALUE,
        lateralDistanceRightMeters = 2.0,
    )

private const val VEHICLE_APPROACH_LEFT_JSON =
    """{"sideBySideLeftVehicleIds":[3],"sideBySideRightVehicleIds":[],""" +
        """"lateralDistanceLeftMeters":1.5,"lateralDistanceRightMeters":1.7976931348623157E308}"""

private const val VEHICLE_APPROACH_RIGHT_JSON =
    """{"sideBySideLeftVehicleIds":[],"sideBySideRightVehicleIds":[5],""" +
        """"lateralDistanceLeftMeters":1.7976931348623157E308,"lateralDistanceRightMeters":2.0}"""

private val overheatingDamage =
    LmuWindowsVehicleDamageData(
        overheating = true,
        partDetached = false,
        lastImpactMagnitude = 0.5,
    )

private val partDetachedDamage =
    LmuWindowsVehicleDamageData(
        overheating = false,
        partDetached = true,
        lastImpactMagnitude = 1.2,
    )

private const val OVERHEATING_DAMAGE_JSON =
    """{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.5}"""

private const val PART_DETACHED_DAMAGE_JSON =
    """{"overheating":false,"partDetached":true,"lastImpactMagnitude":1.2}"""

private val tyreCarcassTemperatureData1 =
    LmuWindowsTyreCarcassTemperatureData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to CelsiusReading(80.0f),
                WheelIndex.FRONT_RIGHT to CelsiusReading(82.0f),
                WheelIndex.REAR_LEFT to CelsiusReading(85.0f),
                WheelIndex.REAR_RIGHT to CelsiusReading(87.0f),
            ),
    )

private val tyreCarcassTemperatureData2 =
    LmuWindowsTyreCarcassTemperatureData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to CelsiusReading(90.0f),
                WheelIndex.FRONT_RIGHT to CelsiusReading(91.0f),
                WheelIndex.REAR_LEFT to CelsiusReading(92.0f),
                WheelIndex.REAR_RIGHT to CelsiusReading(93.0f),
            ),
    )

private const val TYRE_CARCASS_TEMPERATURE_JSON_1 =
    """{"wheels":{"FRONT_LEFT":80.0,"FRONT_RIGHT":82.0,"REAR_LEFT":85.0,"REAR_RIGHT":87.0}}"""

private const val TYRE_CARCASS_TEMPERATURE_JSON_2 =
    """{"wheels":{"FRONT_LEFT":90.0,"FRONT_RIGHT":91.0,"REAR_LEFT":92.0,"REAR_RIGHT":93.0}}"""

private val vehicleClassData1 = LmuWindowsVehicleClassData.fromRawValue("Hypercar")

private val vehicleClassData2 = LmuWindowsVehicleClassData.fromRawValue("LMP2")

private const val VEHICLE_CLASS_JSON_1 = """{"name":"Hypercar"}"""

private const val VEHICLE_CLASS_JSON_2 = """{"name":"LMP2"}"""

private val tyreWearData1 =
    LmuWindowsTyreWearData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to 0.8,
                WheelIndex.FRONT_RIGHT to 0.82,
                WheelIndex.REAR_LEFT to 0.85,
                WheelIndex.REAR_RIGHT to 0.87,
            ),
    )

private val tyreWearData2 =
    LmuWindowsTyreWearData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to 0.5,
                WheelIndex.FRONT_RIGHT to 0.51,
                WheelIndex.REAR_LEFT to 0.52,
                WheelIndex.REAR_RIGHT to 0.53,
            ),
    )

private const val TYRE_WEAR_JSON_1 =
    """{"wheels":{"FRONT_LEFT":0.8,"FRONT_RIGHT":0.82,"REAR_LEFT":0.85,"REAR_RIGHT":0.87}}"""

private const val TYRE_WEAR_JSON_2 =
    """{"wheels":{"FRONT_LEFT":0.5,"FRONT_RIGHT":0.51,"REAR_LEFT":0.52,"REAR_RIGHT":0.53}}"""

// --- Fake リポジトリ ---

private class FakeLmuWindowsFlagRepository : LmuWindowsFlagRepository {
    private val channel = Channel<LmuWindowsRaceFlagsData>(capacity = Channel.UNLIMITED)

    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsRaceFlagsData) {
        channel.trySend(data).getOrThrow()
    }
}

private class FakeLmuWindowsVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    private val channel = Channel<LmuWindowsVehicleApproachData>(capacity = Channel.UNLIMITED)

    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsVehicleApproachData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyLmuWindowsVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
}

private object EmptyLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private class FakeLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    private val channel = Channel<LmuWindowsVehicleDamageData>(capacity = Channel.UNLIMITED)

    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsVehicleDamageData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyLmuWindowsTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private class FakeLmuWindowsTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    private val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(capacity = Channel.UNLIMITED)

    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsTyreCarcassTemperatureData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyLmuWindowsVehicleClassRepository : LmuWindowsVehicleClassRepository {
    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> = emptyFlow()
}

private class FakeLmuWindowsVehicleClassRepository : LmuWindowsVehicleClassRepository {
    private val channel = Channel<LmuWindowsVehicleClassData>(capacity = Channel.UNLIMITED)

    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsVehicleClassData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyLmuWindowsTyreWearRepository : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = emptyFlow()
}

private class FakeLmuWindowsTyreWearRepository : LmuWindowsTyreWearRepository {
    private val channel = Channel<LmuWindowsTyreWearData>(capacity = Channel.UNLIMITED)

    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsTyreWearData) {
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

private val timingTelemetryData1 =
    LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyWheels),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 65_000L,
                lastLapTimeMs = 90_000L,
                bestLapTimeMs = 88_000L,
                sector1Ms = 30_000L,
                sector1And2Ms = 28_000L,
                currentLap = 3,
                maxLaps = 10,
            ),
        vehicle =
            LmuWindowsVehicleData(
                localVelocityX = 0.0,
                localVelocityY = 0.0,
                localVelocityZ = 0.0,
                positionX = 0.0,
                positionY = 0.0,
                positionZ = 0.0,
            ),
    )

private val timingTelemetryData2 =
    timingTelemetryData1.copy(
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 70_000L,
                lastLapTimeMs = 91_000L,
                bestLapTimeMs = 88_000L,
                sector1Ms = 31_000L,
                sector1And2Ms = 29_000L,
                currentLap = 4,
                maxLaps = 10,
            ),
    )

private const val TIMING_JSON_1 =
    """{"currentLapTimeMs":65000,"lastLapTimeMs":90000,"bestLapTimeMs":88000,""" +
        """"sector1Ms":30000,"sector1And2Ms":28000,"currentLap":3,"maxLaps":10}"""

private const val TIMING_JSON_2 =
    """{"currentLapTimeMs":70000,"lastLapTimeMs":91000,"bestLapTimeMs":88000,""" +
        """"sector1Ms":31000,"sector1And2Ms":29000,"currentLap":4,"maxLaps":10}"""

private val virtualEnergyData1 = LmuWindowsVirtualEnergyData(remainingRatio = 0.5, session = 10)
private val virtualEnergyData2 = LmuWindowsVirtualEnergyData(remainingRatio = 0.3, session = 10)

private const val VIRTUAL_ENERGY_JSON_1 = """{"remainingRatio":0.5,"session":10}"""
private const val VIRTUAL_ENERGY_JSON_2 = """{"remainingRatio":0.3,"session":10}"""

private object EmptyLmuWindowsVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = emptyFlow()
}

private class FakeLmuWindowsVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    private val channel = Channel<LmuWindowsVirtualEnergyData>(capacity = Channel.UNLIMITED)

    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsVirtualEnergyData) {
        channel.trySend(data).getOrThrow()
    }
}

private val aceFuelData1 = AceWindowsFuelData(remainingPercent = 42.0)
private val aceFuelData2 = AceWindowsFuelData(remainingPercent = 28.5)

private const val ACE_FUEL_JSON_1 = """{"remainingPercent":42.0}"""
private const val ACE_FUEL_JSON_2 = """{"remainingPercent":28.5}"""

private object EmptyAceWindowsFuelRepository : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false
}

private class FakeAceWindowsFuelRepository : AceWindowsFuelRepository {
    private val channel = Channel<AceWindowsFuelData>(capacity = Channel.UNLIMITED)

    override fun fuelStream(): Flow<AceWindowsFuelData> = channel.receiveAsFlow()

    override suspend fun isConnected(): Boolean = false

    fun emit(data: AceWindowsFuelData) {
        channel.trySend(data).getOrThrow()
    }
}

private val aceFlagData1 = AceWindowsFlagData(flag = AceWindowsFlagType.BLUE_FLAG)
private val aceFlagData2 = AceWindowsFlagData(flag = AceWindowsFlagType.YELLOW_FLAG)

private const val ACE_FLAG_JSON_1 = """{"flag":"BLUE_FLAG"}"""
private const val ACE_FLAG_JSON_2 = """{"flag":"YELLOW_FLAG"}"""

private object EmptyAceWindowsFlagRepository : AceWindowsFlagRepository {
    override fun flagStream(): Flow<AceWindowsFlagData> = emptyFlow()
}

private class FakeAceWindowsFlagRepository : AceWindowsFlagRepository {
    private val channel = Channel<AceWindowsFlagData>(capacity = Channel.UNLIMITED)

    override fun flagStream(): Flow<AceWindowsFlagData> = channel.receiveAsFlow()

    fun emit(data: AceWindowsFlagData) {
        channel.trySend(data).getOrThrow()
    }
}

private val aceStatusData1 = AceWindowsStatusData(status = AceWindowsStatusType.LIVE)
private val aceStatusData2 = AceWindowsStatusData(status = AceWindowsStatusType.PAUSE)

private const val ACE_STATUS_JSON_1 = """{"status":"LIVE","carLocation":"UNASSIGNED"}"""
private const val ACE_STATUS_JSON_2 = """{"status":"PAUSE","carLocation":"UNASSIGNED"}"""

private object EmptyAceWindowsStatusRepository : AceWindowsStatusRepository {
    override fun statusStream(): Flow<AceWindowsStatusData> = emptyFlow()
}

private class FakeAceWindowsStatusRepository : AceWindowsStatusRepository {
    private val channel = Channel<AceWindowsStatusData>(capacity = Channel.UNLIMITED)

    override fun statusStream(): Flow<AceWindowsStatusData> = channel.receiveAsFlow()

    fun emit(data: AceWindowsStatusData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyAceWindowsTyreCarcassTemperatureRepository : AceWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private val pitStatusData1 =
    LmuWindowsPitStatusData(inPits = true, pitState = LmuWindowsPitState.ENTERING, inGarageStall = false)
private val pitStatusData2 =
    LmuWindowsPitStatusData(inPits = false, pitState = LmuWindowsPitState.NONE, inGarageStall = true)

private const val PIT_STATUS_JSON_1 = """{"inPits":true,"pitState":"ENTERING","inGarageStall":false}"""
private const val PIT_STATUS_JSON_2 = """{"inPits":false,"pitState":"NONE","inGarageStall":true}"""

private object EmptyLmuWindowsPitStatusRepository : LmuWindowsPitStatusRepository {
    override fun pitStatusStream(): Flow<LmuWindowsPitStatusData> = emptyFlow()
}

private class FakeLmuWindowsPitStatusRepository : LmuWindowsPitStatusRepository {
    private val channel = Channel<LmuWindowsPitStatusData>(capacity = Channel.UNLIMITED)

    override fun pitStatusStream(): Flow<LmuWindowsPitStatusData> = channel.receiveAsFlow()

    fun emit(data: LmuWindowsPitStatusData) {
        channel.trySend(data).getOrThrow()
    }
}
