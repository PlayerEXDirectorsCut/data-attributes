package com.bms.data_attributes.networking

import com.bms.data_attributes.DataAttributes
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object NetworkingChannels {
    @JvmField
    val HANDSHAKE = DataAttributes.id("handshake")
    @JvmField
    val RELOAD = DataAttributes.id("reload")
}