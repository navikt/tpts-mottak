package no.nav.tpts.mottak.soknad.soknadList

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.async
import no.nav.tpts.mottak.common.pagination.PageData
import no.nav.tpts.mottak.common.pagination.paginate
import no.nav.tpts.mottak.soknad.SoknadQueries.countSoknader
import no.nav.tpts.mottak.soknad.SoknadQueries.listSoknader
import org.apache.logging.log4j.kotlin.logger

private val LOG = logger("no.nav.tpts.mottak.soknad.soknadList.SoknadListRoute")

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
