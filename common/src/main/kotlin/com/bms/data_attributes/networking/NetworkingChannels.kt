package com.bms.data_attributes.networking

import com.bms.data_attributes.DataAttributes
import io.wispforest.owo.network.OwoNetChannel
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object NetworkingChannels {
    val HANDSHAKE = OwoNetChannel.create(DataAttributes.id("handshake"))
    @JvmField
    val RELOAD = OwoNetChannel.create(DataAttributes.id("reload"))
}