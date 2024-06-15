package com.bibireden.data_attributes.serde

import com.bibireden.data_attributes.config.OverridesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.data.AttributeFunction
import io.wispforest.endec.Endec
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import io.wispforest.owo.network.serialization.PacketBufSerializer
import kotlin.reflect.KClass

object PacketBufs {
    private fun <T : Any> registerSerializer(clazz: KClass<T>, endec: Endec<T>) = PacketBufSerializer.register(clazz.java,
        { buf, t -> endec.encodeFully({ ByteBufSerializer.of(buf) }, t); return@register },
        { buf -> endec.decodeFully(ByteBufDeserializer::of, buf) }
    )

    fun registerPacketSerializers() {
        registerSerializer(AttributeOverrideConfig::class, AttributeOverrideConfig.ENDEC)
        registerSerializer(AttributeFunction::class, AttributeFunction.ENDEC)
    }
}