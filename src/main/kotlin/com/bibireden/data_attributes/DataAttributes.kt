package com.bibireden.data_attributes

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents
import com.bibireden.data_attributes.data.AttributeResourceManager
import com.bibireden.data_attributes.endec.nbt.NbtSerializer
import com.bibireden.data_attributes.mutable.MutableAttributeContainer
import io.wispforest.endec.format.bytebuf.ByteBufSerializer
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
import net.minecraft.nbt.NbtCompound
import net.minecraft.resource.ResourceType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.world.World

class DataAttributes : ModInitializer {
    companion object {
        @JvmField val HANDSHAKE = DataAttributesAPI.id("handshake")
        @JvmField val RELOAD = DataAttributesAPI.id("reload")

        @JvmField var CLIENT_MANAGER = AttributeResourceManager()
        @JvmField val SERVER_MANAGER = AttributeResourceManager()

        /** Gets the [AttributeResourceManager] based on the world context. */
        fun getManager(world: World): AttributeResourceManager = when {
            world.isClient -> CLIENT_MANAGER
            else -> SERVER_MANAGER
        }

        fun loginQueryStart(handler: ServerLoginNetworkHandler, server: MinecraftServer, sender: PacketSender, synchronizer: LoginSynchronizer) {
            sender.sendPacket(HANDSHAKE, AttributeResourceManager.ENDEC.encodeFully({ -> ByteBufSerializer.of(PacketByteBufs.create())}, SERVER_MANAGER))
        }

        @JvmStatic
        fun refreshAttributes(entity: Entity) {
            if (entity is LivingEntity) (entity.attributes as MutableAttributeContainer).refresh()
        }

        fun onHealthModified(attribute: EntityAttribute, entity: LivingEntity?, modifier: EntityAttributeModifier?, previous: Double, added: Boolean) {
            if (entity?.world?.isClient == false && attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
                entity.health = (entity.health * entity.maxHealth / previous).toFloat()
            }
        }
    }

    override fun onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(SERVER_MANAGER)

        ServerLoginNetworking.registerGlobalReceiver(HANDSHAKE) { _, _, _, _, _, _ -> }

        ServerLoginConnectionEvents.QUERY_START.register(::loginQueryStart)

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, newEntity, _, _ -> refreshAttributes(newEntity) }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> refreshAttributes(player) }

        EntityAttributeModifiedEvents.MODIFIED.register(::onHealthModified)
    }
}