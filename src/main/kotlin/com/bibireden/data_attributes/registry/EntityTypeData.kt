package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.mutable.MutableDefaultAttributeContainer
import com.bibireden.data_attributes.utils.NbtIO
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class EntityTypeData(val data: MutableMap<Identifier, Double> = mutableMapOf()) : NbtIO {
    fun build(builder: DefaultAttributeContainer.Builder, container: DefaultAttributeContainer?) {
        if (container != null) {
            (container as MutableDefaultAttributeContainer).copy(builder)
        }

        this.data.keys.forEach { key ->
            val attribute = Registries.ATTRIBUTE[key] ?: return@forEach
            builder.add(attribute, attribute.clamp(this.data[key] ?: return@forEach))
        }
    }

    override fun readFromNbt(tag: NbtCompound) {
        tag.keys.forEach { key -> this.data[Identifier(key)] = tag.getDouble(key) }
    }

    override fun writeToNbt(tag: NbtCompound) {
        this.data.forEach { (key, value) -> tag.putDouble(key.toString(), value) }
    }
}
