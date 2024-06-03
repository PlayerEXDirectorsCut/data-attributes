package com.bibireden.data_attributes.data.merged

import com.bibireden.data_attributes.data.AttributeFunction
import io.wispforest.endec.Endec

class AttributeFunctions(values: Map<String, Map<String, AttributeFunction>>) : DataMerger<AttributeFunction>(values) {
    companion object {
        @JvmField
        val ENDEC: Endec<AttributeFunctions> = AttributeFunction.ENDEC.mapOf().mapOf().xmap(::AttributeFunctions) { it.values }
    }
}