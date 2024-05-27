package com.bibireden.data_attributes.utils

import net.minecraft.nbt.NbtCompound

interface NbtIO {
    fun readFromNbt(tag: NbtCompound)
    fun writeToNbt(tag: NbtCompound)
}