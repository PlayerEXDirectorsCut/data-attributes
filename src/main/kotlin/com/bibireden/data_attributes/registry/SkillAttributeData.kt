package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.utils.NbtIO
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class SkillAttributeData(
    private var attributeOverride: AttributeOverrideData? = null,
    private val skills: MutableMap<Identifier, AttributeSkillData> = mutableMapOf(),
) : NbtIO {
    fun override(identifier: Identifier, func: (Identifier, EntityAttribute) -> EntityAttribute) {
        this.attributeOverride?.let { override ->
            override.override(func(identifier, override.create()) as MutableEntityAttribute)
        }
    }

    fun copy(entityAttributeIn: EntityAttribute) {
        val mutableEntityAttribute = entityAttributeIn as MutableEntityAttribute
        this.skills.keys.forEach { key ->
            val entityAttribute = Registries.ATTRIBUTE[key] ?: return@forEach
            mutableEntityAttribute.addChild(entityAttribute as MutableEntityAttribute, skills[key] ?: return@forEach)
        }
    }

    fun putSkills(functions: Map<Identifier, AttributeSkillData>) {
        this.skills.putAll(functions)
    }

    override fun readFromNbt(tag: NbtCompound) {
        if (tag.contains("attribute_override")) {
            this.attributeOverride = AttributeOverrideData(tag.getCompound("attribute_override"))
        }

        val nbtSkills = tag.getCompound("skill")
        nbtSkills.keys.forEach { key ->
            this.skills[Identifier(key)] = AttributeSkillData(nbtSkills.getDouble(key))
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        val overrideNbt = NbtCompound()

        this.attributeOverride?.let { override ->
            override.writeToNbt(overrideNbt)
            tag.put("attribute_override", overrideNbt)
        }

        val skillsNbt = NbtCompound()
        this.skills.forEach { (key, value) ->
            skillsNbt.putDouble(key.toString(), value.value())
            tag.put("skill", skillsNbt)
        }
    }
}