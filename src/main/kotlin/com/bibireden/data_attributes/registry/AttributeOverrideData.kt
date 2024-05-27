package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.api.enums.FunctionBehavior
import com.bibireden.data_attributes.api.enums.StackingFormula
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.utils.NbtIO
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.nbt.NbtCompound

/** A segment of data pertaining to a specific attribute meant to be overrided in the game. */
class AttributeOverrideData(
    var default: Double,
    var min: Double,
    var max: Double,
    var increment: Double,
    var behavior: FunctionBehavior,
    var formula: StackingFormula,
    var translationKey: String
) : NbtIO {
    companion object {
        val CODEC: Codec<AttributeOverrideData> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.DOUBLE.fieldOf("default").forGetter { it.default },
                Codec.DOUBLE.fieldOf("min").forGetter { it.min },
                Codec.DOUBLE.fieldOf("max").forGetter { it.max },
                Codec.DOUBLE.fieldOf("increment").forGetter { it.increment },
                Codec.STRING.xmap(FunctionBehavior::of) { x -> x.name.uppercase() }
                    .fieldOf("behavior").forGetter { it.behavior },
                Codec.STRING.xmap(StackingFormula::of) { x -> x.name.uppercase() }
                    .fieldOf("formula").forGetter { it.formula },
                Codec.STRING.fieldOf("translationKey").forGetter { it.translationKey },
            ).apply(builder, ::AttributeOverrideData)
        }
    }

    constructor(tag: NbtCompound) : this(
        tag.getDouble("default"),
        tag.getDouble("min"),
        tag.getDouble("max"),
        tag.getDouble("increment"),
        FunctionBehavior.entries[tag.getByte("behavior").toInt()],
        StackingFormula.entries[tag.getByte("formula").toInt()],
        tag.getString("translationKey")
    )

    /** Creates an entity attribute based on this override. */
    fun create(): EntityAttribute = ClampedEntityAttribute(translationKey, this.default, this.min, this.max)
    /** Overrides a `MutableEntityAttribute` via. itself. */
    fun override(attribute: MutableEntityAttribute) = attribute.override(this)

    override fun readFromNbt(tag: NbtCompound) {
        this.default = tag.getDouble("default")
        this.min = tag.getDouble("min")
        this.max = tag.getDouble("max")
        this.increment = tag.getDouble("increment")
        this.behavior = FunctionBehavior.entries[tag.getByte("behavior").toInt()]
        this.formula = StackingFormula.entries[tag.getByte("formula").toInt()]
        this.translationKey = tag.getString("translationKey")
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.putDouble("default", this.default)
        tag.putDouble("min", this.min)
        tag.putDouble("max", this.max)
        tag.putDouble("increment", this.increment)
        tag.putByte("behavior", this.behavior.ordinal.toByte())
        tag.putByte("formula", this.formula.ordinal.toByte())
        tag.putString("translationKey", this.translationKey)
    }
}