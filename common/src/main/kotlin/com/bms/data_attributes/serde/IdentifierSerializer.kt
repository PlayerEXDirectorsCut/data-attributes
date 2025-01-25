package com.bms.data_attributes.serde

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation

class ResourceLocationSerializer : KSerializer<ResourceLocation> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ResourceLocation", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ResourceLocation {
        val str = decoder.decodeString()
        return ResourceLocation.tryParse(str) ?: throw SerializationException("failed to parse identifier $str")
    }

    override fun serialize(encoder: Encoder, value: ResourceLocation) = encoder.encodeString(value.toString())
}