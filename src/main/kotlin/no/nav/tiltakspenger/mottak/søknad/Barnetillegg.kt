package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import java.time.LocalDate

@Serializable
data class Barnetillegg(
    // fra søknaden kommer enten ident (om barn er forhåndsutfylt) eller fødselsdato (om barn er manuelt lagt til)
    val ident: String? = null,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fødselsdato: LocalDate? = null,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val alder: Int,
    val oppholdsland: String,
    val søktBarnetillegg: Boolean,
)
