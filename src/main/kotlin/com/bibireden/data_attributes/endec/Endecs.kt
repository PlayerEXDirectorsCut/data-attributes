package com.bibireden.data_attributes.endec

import io.wispforest.endec.Endec
import net.minecraft.util.Identifier

object Endecs {
    @JvmField
    val IDENTIFIER: Endec<Identifier> = Endec.STRING.xmap({ Identifier.tryParse(it)!! }, Identifier::toString)
}