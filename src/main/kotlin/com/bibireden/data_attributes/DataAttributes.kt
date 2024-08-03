package com.bibireden.data_attributes

import com.bibireden.data_attributes.config.*
import com.bibireden.data_attributes.config.models.DataAttributesConfig
import com.bibireden.data_attributes.config.models.EntityTypesConfig
import com.bibireden.data_attributes.config.models.FunctionsConfig
import com.bibireden.data_attributes.config.models.OverridesConfig
import com.bibireden.data_attributes.ext.refreshAttributes
import com.bibireden.data_attributes.networking.NetworkingChannels
import com.bibireden.data_attributes.networking.ConfigPacketBufs
import com.bibireden.data_attributes.serde.JanksonBuilders
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data_attributes"

        @JvmField val LOGGER: Logger = LogManager.getLogger()

        @JvmField val CONFIG: DataAttributesConfig = DataAttributesConfig.createAndLoad()
        @JvmField val OVERRIDES_CONFIG: OverridesConfig = OverridesConfig.createAndLoad()
        @JvmField val FUNCTIONS_CONFIG: FunctionsConfig = FunctionsConfig.createAndLoad(JanksonBuilders::applyFunctions)
        @JvmField val ENTITY_TYPES_CONFIG: EntityTypesConfig = EntityTypesConfig.createAndLoad(JanksonBuilders::applyEntityTypes)


        val MANAGER = AttributeConfigManager()

        /** Creates an [Identifier] associated with the [MOD_ID]. */
        fun id(str: String) = Identifier.of(MOD_ID, str)!!

        /** Acquires the proper manager based on the world. */
        fun getManagerFromWorld(world: World) = if (world.isClient) DataAttributesClient.MANAGER else MANAGER

        /** Reload all the data-attributes configs at once. */
        @JvmStatic
        fun reloadConfigs() {
            OVERRIDES_CONFIG.load()
            FUNCTIONS_CONFIG.load()
            ENTITY_TYPES_CONFIG.load()
            CONFIG.load()
        }

        /** Save all the data-attributes configs at once. */
        @JvmStatic
        fun saveConfigs() {
            OVERRIDES_CONFIG.save()
            FUNCTIONS_CONFIG.save()
            ENTITY_TYPES_CONFIG.save()
            CONFIG.save()
        }

        init {
            ConfigPacketBufs.registerPacketSerializers()
        }
    }

    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(AttributeConfigManager.Packet.PACKET_ID, AttributeConfigManager.Packet.PACKET_CODEC)

        ServerLoginNetworking.registerGlobalReceiver(NetworkingChannels.HANDSHAKE) { _, _, _, _, _, _ -> }

        ServerLoginConnectionEvents.INIT.register { _, _ -> if (CONFIG.applyOnWorldStart) MANAGER.update() }
        ServerLoginConnectionEvents.QUERY_START.register { _, _, sender, _ ->
            sender.sendPacket(NetworkingChannels.HANDSHAKE, AttributeConfigManager.Packet.ENDEC.encodeFully({ ByteBufSerializer.of(PacketByteBufs.create()) }, MANAGER.toPacket()))
        }

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, current, _, _ ->
            if (current is LivingEntity) current.refreshAttributes()
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> player.refreshAttributes() }
    }
}