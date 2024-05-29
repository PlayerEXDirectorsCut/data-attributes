package com.bibireden.data_attributes.utils

import net.minecraft.nbt.NbtCompound

/** Interface to be applied to certain classes/interfaces for `nbt` manipulation. */
interface NbtIO {
    /** Reading from a `nbt` parameter. */
    fun readFromNbt(tag: NbtCompound)
    /** Writing to a `nbt` parameter. */
    fun writeToNbt(tag: NbtCompound)
}