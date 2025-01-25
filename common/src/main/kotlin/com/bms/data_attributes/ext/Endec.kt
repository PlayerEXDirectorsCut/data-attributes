package com.bms.data_attributes.ext

import io.wispforest.endec.Endec

/** Maps the [Endec] as a key to another which is meant to represent a value. */
fun <K, V> Endec<K>.keyOf(other: Endec<V>): Endec<Map<K, V>> = Endec.map(this, other)
fun <K, V> Endec<K>.keyOfMutable(other: Endec<V>): Endec<MutableMap<K, V>> = Endec.map(this, other)