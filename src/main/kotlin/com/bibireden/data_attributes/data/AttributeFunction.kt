package com.bibireden.data_attributes.data

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonObject
import blue.endless.jankson.annotation.Deserializer
import blue.endless.jankson.annotation.Serializer
import com.bibireden.data_attributes.api.attribute.IAttributeFunction
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.util.Maths
import com.bibireden.data_attributes.endec.Endecs
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.util.Identifier

/** Represents a function normally tied to an `Identifier` that contains a `StackingBehavior` and a `Double` value. */
open class AttributeFunction(open var behavior: StackingBehavior, open var value: Double) : IAttributeFunction {
    companion object {
        @JvmField
        val ENDEC = StructEndecBuilder.of(
            Endec.STRING.xmap(StackingBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior") { it.behavior },
            Endec.DOUBLE.fieldOf("value") { it.value },
            ::AttributeFunction
        )
    }

    override fun behavior(): StackingBehavior = this.behavior

    override fun value(): Double = this.value
}

data class AttributeFunctionConfig(var id: Identifier, override var behavior: StackingBehavior, override var value: Double) : AttributeFunction(behavior, value)
{
    constructor() : this(Identifier("..."), StackingBehavior.Add, 0.0)
    companion object {
        val ENDEC = StructEndecBuilder.of(
            Endecs.IDENTIFIER.fieldOf("id") { it.id },
            Endec.STRING.xmap(StackingBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior") { it.behavior },
            Endec.DOUBLE.fieldOf("value") { it.value },
            ::AttributeFunctionConfig
        )
    }
}

data class AttributeFunctionConfigData(var data: Map<Identifier, List<AttributeFunctionConfig>> = mapOf()) {
    companion object {
        val ENDEC = Endec.map(Endecs.IDENTIFIER, AttributeFunctionConfig.ENDEC.listOf())
            .xmap(::AttributeFunctionConfigData) { it.data }
        
    }
}