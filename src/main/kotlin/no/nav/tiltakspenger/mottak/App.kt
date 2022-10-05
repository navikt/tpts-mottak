package no.nav.tiltakspenger.mottak

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.Configuration.KafkaConfig
import no.nav.tiltakspenger.mottak.health.healthRoutes
import no.nav.tiltakspenger.mottak.joark.JoarkReplicator
import no.nav.tiltakspenger.mottak.joark.createKafkaConsumer
import no.nav.tiltakspenger.mottak.joark.createKafkaProducer
import no.nav.tiltakspenger.mottak.saf.SafClient
import no.nav.tiltakspenger.mottak.saf.SafService

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }
    log.info { "starting server" }
    unleash // init
    val kafkaConfig = KafkaConfig()
    val joarkReplicator =
        JoarkReplicator(
            consumer = createKafkaConsumer(config = kafkaConfig),
            producer = createKafkaProducer(config = kafkaConfig),
            safService = SafService(safClient = SafClient(config = Configuration.SafConfig())),
            tptsRapidName = Configuration.tptsRapidName()
        ).also { it.start() }

    val server = embeddedServer(Netty, Configuration.applicationPort()) {
        routing {
            healthRoutes(listOf(joarkReplicator))
        }
    }.start(wait = true)

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info { "stopping server" }
        server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
    })
}
