package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.api.attribute.IAttributeFunction
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.util.Maths
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder

class AttributeFunction(val behavior: StackingBehavior, val value: Double) : IAttributeFunction {
    companion object {
        val endec = StructEndecBuilder.of(
            Endec.STRING.xmap(StackingBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior", { it.behavior }),
            Endec.DOUBLE.fieldOf("value") { it.value },
            ::AttributeFunction
        )

        fun read(array: ByteArray) = AttributeFunction(StackingBehavior.of(array[8]), Maths.byteArrayToDouble(array))
    }

    fun write(): ByteArray {
        val array = Maths.doubleToByteArray(this.value, 9)
        array[8] = this.behavior.id()
        return array
    }

    override fun behavior(): StackingBehavior = this.behavior

    override fun value(): Double = this.value
}