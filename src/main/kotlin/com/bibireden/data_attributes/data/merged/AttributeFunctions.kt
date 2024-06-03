package com.bibireden.data_attributes.data.merged

import com.bibireden.data_attributes.data.AttributeFunction
import io.wispforest.endec.Endec

class AttributeFunctions(values: Map<String, Map<String, AttributeFunction>>) : DataMerger<AttributeFunction>(values) {
    companion object {
        val endec: Endec<AttributeFunctions> = AttributeFunction.endec.mapOf().mapOf().xmap(::AttributeFunctions) { it.values }
    }
}