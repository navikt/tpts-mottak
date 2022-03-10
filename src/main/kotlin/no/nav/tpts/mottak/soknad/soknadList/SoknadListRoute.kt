package no.nav.tpts.mottak.soknad.soknadList

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.async
import kotliquery.queryOf
import no.nav.tpts.mottak.common.pagination.PageData
import no.nav.tpts.mottak.common.pagination.paginate
import no.nav.tpts.mottak.db.DataSource

fun Route.soknadListRoute() {
    route("/api/soknad") {
        get {
            paginate { offset, pageSize ->
                val total = async { countSoknader() }
                val soknader = async { listSoknader(pageSize = pageSize, offset = offset) }
                return@paginate PageData(data = soknader.await(), total = total.await() ?: 0)
            }
        }
    }
}
