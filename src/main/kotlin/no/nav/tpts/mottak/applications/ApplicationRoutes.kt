package no.nav.tpts.mottak.applications

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import no.nav.tpts.mottak.LOG
import java.lang.Exception

val JWTPrincipal.userId: String
    get() = this.subject ?: throw Exception("No user subject claim found on token")

fun Route.applicationRoutes() {
    route("/api/test") {
        get {
            call.respondText("OK")
            LOG.info("Test endpoint")
            val headers = call.request.headers.toMap()
            LOG.info(headers.toString())
            LOG.info("Auth: ${headers["Authorization"]}")
        }
    }
    authenticate("auth-jwt") {
        route("/api/application") {
            get {
                call.respondText("OK")
                val principal =  call.principal<JWTPrincipal>()
                LOG.info(principal!!.payload.claims.toString())
                LOG.info(principal!!.userId)
            }
        }
    }
}