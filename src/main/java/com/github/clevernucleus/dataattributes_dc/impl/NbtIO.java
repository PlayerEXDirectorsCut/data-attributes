package com.github.clevernucleus.dataattributes_dc.impl;

import net.minecraft.nbt.NbtCompound;

public interface NbtIO {
	void readFromNbt(NbtCompound tag);
	void writeToNbt(NbtCompound tag);
}
