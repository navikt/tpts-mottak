package no.nav.tiltakspenger.mottak.saf

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SafServiceTest {
    private val safClient = mockk<SafClient>()
    private val safService = SafService(safClient)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no metadata is found, no document is retreived`() = runTest {
        // given
        val journalpostId = "42"
        coEvery { safClient.hentMetadataForJournalpost(journalpostId) }.returns(null)

        // when
        val soknad = safService.hentSøknad(journalpostId)

        // then
        assertNull(soknad)
        coVerify(exactly = 1) { safClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 0) { safClient.hentSoknad(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when metadata is found, a document is retreived`() = runTest {
        // given
        val journalpostId = "42"
        val dokumentInfoId = "43"
        val rawJson = this::class.java.classLoader.getResource("søknad.json")!!.readText()
        val journalfortDokumentMetadata = JournalfortDokumentMetadata(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = "tiltakspenger.json",
        )
        coEvery { safClient.hentMetadataForJournalpost(journalpostId) }.returns(
            journalfortDokumentMetadata,
        )
        coEvery { safClient.hentSoknad(journalfortDokumentMetadata) }.returns(rawJson)

        // when
        val soknad = safService.hentSøknad(journalpostId)

        val mappedJson = jacksonObjectMapper().readTree(soknad?.søknad) as ObjectNode

        // then
        assertEquals("12304", mappedJson.path("søknadId").asText())
        coVerify(exactly = 1) { safClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 1) { safClient.hentSoknad(journalfortDokumentMetadata) }
    }
}
