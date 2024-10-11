package com.bibireden.data_attributes.serde

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.Identifier

class IdentifierSerializer : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Identifier {
        val str = decoder.decodeString()
        return Identifier.tryParse(str) ?: throw SerializationException("failed to parse identifier $str")
    }

    override fun serialize(encoder: Encoder, value: Identifier) = encoder.encodeString(value.toString())
}