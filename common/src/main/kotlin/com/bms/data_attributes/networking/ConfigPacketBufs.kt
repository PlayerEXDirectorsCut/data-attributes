package com.bms.data_attributes.networking

import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.config.entities.EntityTypeData
import com.bms.data_attributes.config.entities.EntityTypeEntry
import io.wispforest.endec.Endec
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import io.wispforest.endec.impl.ReflectiveEndecBuilder
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KClass

/** All serializers/deserializers related to [ByteBufSerializer] for the Data Attributes config. */
@ApiStatus.Internal
object ConfigPacketBufs {
    private fun <T : Any> registerSerializer(clazz: KClass<T>, endec: Endec<T>) = ReflectiveEndecBuilder.SHARED_INSTANCE.register(endec, clazz.java)

    fun registerPacketSerializers() {
        registerSerializer(AttributeOverride::class, AttributeOverride.ENDEC)
        registerSerializer(AttributeFunction::class, AttributeFunction.ENDEC)
        registerSerializer(EntityTypeData::class, EntityTypeData.ENDEC)
        registerSerializer(EntityTypeEntry::class, EntityTypeEntry.ENDEC)
    }
}