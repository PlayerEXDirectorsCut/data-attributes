package com.bibireden.data_attributes.networking

import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.config.entities.EntityTypeEntry
import io.wispforest.endec.Endec
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import io.wispforest.owo.network.serialization.PacketBufSerializer
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KClass

/** All serializers/deserializers related to [ByteBufSerializer] for the Data Attributes config. */
@ApiStatus.Internal
object ConfigPacketBufs {
    private fun <T : Any> registerSerializer(clazz: KClass<T>, endec: Endec<T>) = PacketBufSerializer.register(clazz.java,
        { buf, t -> endec.encodeFully({ ByteBufSerializer.of(buf) }, t); return@register },
        { buf -> endec.decodeFully(ByteBufDeserializer::of, buf) }
    )

    fun registerPacketSerializers() {
        registerSerializer(AttributeOverride::class, AttributeOverride.ENDEC)
        registerSerializer(AttributeFunction::class, AttributeFunction.ENDEC)
        registerSerializer(EntityTypeData::class, EntityTypeData.ENDEC)
        registerSerializer(EntityTypeEntry::class, EntityTypeEntry.ENDEC)
    }
}