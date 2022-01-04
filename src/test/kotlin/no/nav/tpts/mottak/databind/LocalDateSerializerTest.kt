package no.nav.tpts.mottak.databind

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LocalDateSerializerTest {

    @Serializable
    data class ClassForTest(@Serializable(with = LocalDateSerializer::class) val dato: LocalDate)

    @Test
    fun `Serializes LocalDate`() {
        // given
        val dato = LocalDate.of(2022, Month.JANUARY, 1)
        val classForTest = ClassForTest(dato)

        // when
        val json = Json.encodeToString(classForTest)

        // then
        JSONAssert.assertEquals("{dato:2022-01-01}", json, JSONCompareMode.LENIENT)
    }

    @Test
    fun `Deserializes LocalDate`() {
        // given
        val dato = "2022-01-01"
        val json = """{"dato":"$dato"}"""

        // when
        val classForTest = Json.decodeFromString<ClassForTest>(json)

        // then
        assertEquals(dato, classForTest.dato.toString())
    }
}
