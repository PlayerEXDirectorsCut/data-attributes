package com.bibireden.data_attributes

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonObject
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents
import com.bibireden.data_attributes.config.AttributeConfigManager
import com.bibireden.data_attributes.config.DataAttributesConfig
import com.bibireden.data_attributes.data.AttributeFunction
import com.bibireden.data_attributes.data.AttributeFunctionConfig
import com.bibireden.data_attributes.data.AttributeFunctionConfigData
import com.bibireden.data_attributes.data.AttributeResourceManager
import com.bibireden.data_attributes.mutable.MutableAttributeContainer
import com.bibireden.data_attributes.networking.Channels
import com.bibireden.data_attributes.serde.PacketBufs
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
import io.wispforest.endec.format.json.JsonDeserializer
import io.wispforest.endec.format.json.JsonSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking.LoginSynchronizer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.resource.ResourceType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Type

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data_attributes"

        val LOGGER = LogManager.getLogger()

        @JvmField val CONFIG_MANAGER = AttributeConfigManager()

        @JvmField var CLIENT_MANAGER = AttributeResourceManager()
        @JvmField val SERVER_MANAGER = AttributeResourceManager()

        init {
            PacketBufs.registerPacketSerializers()
        }

        fun id(str: String) = Identifier.of(MOD_ID, str)!!

        val CONFIG = DataAttributesConfig.createAndLoad()

        /** Gets the [AttributeResourceManager] based on the world context. */
        fun getManager(world: World): AttributeResourceManager = when {
            world.isClient -> CLIENT_MANAGER
            else -> SERVER_MANAGER
        }

        fun loginQueryStart(handler: ServerLoginNetworkHandler, server: MinecraftServer, sender: PacketSender, synchronizer: LoginSynchronizer) {
            sender.sendPacket(Channels.HANDSHAKE, AttributeResourceManager.ENDEC.encodeFully({ ByteBufSerializer.of(PacketByteBufs.create()) }, SERVER_MANAGER))
        }

        @JvmStatic
        fun refreshAttributes(entity: Entity) {
            if (entity is LivingEntity) {
                (entity.attributes as MutableAttributeContainer).`data_attributes$refresh`()
            }
        }

        fun onHealthModified(attribute: EntityAttribute, entity: LivingEntity?, modifier: EntityAttributeModifier?, previous: Double, added: Boolean) {
            if (entity?.world?.isClient == false && attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
                entity.health = (entity.health * entity.maxHealth / previous).toFloat()
            }
        }
    }

    override fun onInitialize() {
        CONFIG.subscribeToOverrides { data ->
            LOGGER.info("Data Changed: $data")
//            CONFIG_MANAGER.onDataUpdate(data)
        }

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(SERVER_MANAGER)

        ServerLoginNetworking.registerGlobalReceiver(Channels.HANDSHAKE) { _, _, _, _, _, _ -> }

        ServerLoginConnectionEvents.QUERY_START.register(::loginQueryStart)

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, newEntity, _, _ -> refreshAttributes(newEntity) }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> refreshAttributes(player) }

        EntityAttributeModifiedEvents.MODIFIED.register(::onHealthModified)
    }
}