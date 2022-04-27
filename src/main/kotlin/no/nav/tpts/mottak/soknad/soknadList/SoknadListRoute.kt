package no.nav.tpts.mottak.soknad.soknadList

import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.async
import mu.KotlinLogging
import no.nav.tpts.mottak.common.pagination.PageData
import no.nav.tpts.mottak.common.pagination.paginate
import no.nav.tpts.mottak.soknad.SoknadQueries.countSoknader
import no.nav.tpts.mottak.soknad.SoknadQueries.listSoknader

private val LOG = KotlinLogging.logger {}

fun Route.soknadListRoute() {
    route("/api/soknad") {
        get {
            val ident = call.request.queryParameters["ident"]
            paginate { offset, pageSize ->
                val total = async { countSoknader() }
                val soknader = async { listSoknader(pageSize = pageSize, offset = offset, ident = ident) }
                return@paginate PageData(data = soknader.await(), total = total.await() ?: 0)
            }
        }
    }.also { LOG.info { "setting up endpoint /api/soknad" } }
}
