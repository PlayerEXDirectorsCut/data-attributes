package com.bibireden.data_attributes.registry

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.UnboundedMapCodec
import net.minecraft.util.Identifier

/** General attribute data meant to link from an `Identifier` to a map of `Identifiers` that map to a `Double`. */
typealias GeneralAttributeDataType = MutableMap<Identifier, MutableMap<Identifier, Double>>

/**
 * Contains the needed codec to represent attribute data.
 * 
 * @see GeneralAttributeDataType
 * */
object AttributeData {
    val CODEC: UnboundedMapCodec<Identifier, MutableMap<Identifier, Double>> = Codec.unboundedMap(Identifier.CODEC, Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE))
}