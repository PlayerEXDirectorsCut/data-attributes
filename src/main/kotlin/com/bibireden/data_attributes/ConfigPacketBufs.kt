package com.bibireden.data_attributes

import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.functions.AttributeFunctionConfig
import com.bibireden.data_attributes.data.EntityTypeData
import io.wispforest.endec.Endec
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import io.wispforest.owo.network.serialization.PacketBufSerializer
import kotlin.reflect.KClass

/** All serializers/deserializers related to [ByteBufSerializer] for the Data Attributes config. */
object ConfigPacketBufs {
    private fun <T : Any> registerSerializer(clazz: KClass<T>, endec: Endec<T>) = PacketBufSerializer.register(clazz.java,
        { buf, t -> endec.encodeFully({ ByteBufSerializer.of(buf) }, t); return@register },
        { buf -> endec.decodeFully(ByteBufDeserializer::of, buf) }
    )

    fun registerPacketSerializers() {
        registerSerializer(AttributeOverride::class, AttributeOverride.ENDEC)
        registerSerializer(AttributeFunction::class, AttributeFunction.ENDEC)
        registerSerializer(AttributeFunctionConfig::class, AttributeFunctionConfig.ENDEC)
        registerSerializer(EntityTypeData::class, EntityTypeData.ENDEC)
    }
}