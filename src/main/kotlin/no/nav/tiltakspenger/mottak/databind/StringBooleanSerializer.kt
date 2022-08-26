package no.nav.tiltakspenger.mottak.databind

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringBooleanSerializer : KSerializer<Boolean> {
    private val sant = Regex("true|ja", RegexOption.IGNORE_CASE)
    private val usant = Regex("false|nei", RegexOption.IGNORE_CASE)
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringBoolean", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean {
        val verdi = decoder.decodeString()
        return when {
            verdi.matches(sant) -> true
            verdi.matches(usant) -> false
            else -> throw IllegalArgumentException("kun lov med true/ja/false/nei")
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        TODO("Not yet implemented")
    }
}
