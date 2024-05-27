package com.bibireden.data_attributes

import com.bibireden.data_attributes.api.events.EntityAttributeModifiedEvents
import com.bibireden.data_attributes.mutable.MutableAttributeContainer
import com.bibireden.data_attributes.registry.*
import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data-attributes"

        val logger = LoggerFactory.getLogger(MOD_ID)
        val registry = DataAttributesRegistry()

        object Keys {
            val ATTRIBUTES = RegistryKey.ofRegistry<Map<Identifier, Dynamic<*>>>(Identifier("attributes"))
        }

        fun refreshAttributes(entity: Entity) {
            if (entity is LivingEntity) (entity.attributes as MutableAttributeContainer).refresh()
        }

        fun onHealthModified(attribute: EntityAttribute, entity: LivingEntity?, modifier: EntityAttributeModifier?, previous: Double, isAdded: Boolean) {
            if (entity == null || entity.world.isClient || attribute != EntityAttributes.GENERIC_MAX_HEALTH) return
            entity.health = (entity.health * entity.maxHealth / previous).toFloat()
        }
    }

    override fun onInitialize() {
        DynamicRegistries.registerSynced(Keys.ATTRIBUTES, Codec.unboundedMap(Identifier.CODEC, Codec.PASSTHROUGH), DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY)

        DynamicRegistrySetupCallback.EVENT.register { view ->
            view.registerEntryAdded(Keys.ATTRIBUTES) { _, fileID, obj ->
                val skillCache: MutableMap<Identifier, MutableMap<Identifier, Double>> = mutableMapOf()

                registry.clear()

                for ((attributeID, entry) in obj) {
                    // General Attribute Data (will have to be differentiated somehow)
                    if (fileID.path.contains("attribute_overrides")) {
                        // Attribute Override Data
                        entry.decode(AttributeOverrideData.CODEC).resultOrPartial({ x -> logger.error("override fail! :: {}", x)}).ifPresent {
                            registry.skillAttributes[attributeID] = SkillAttributeData(it.first)
                        }
                    }
                    else {
                        entry.decode(Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE)).result().ifPresent {
                            val map = it.first
                            if (fileID.path.contains("skills")) {
                                skillCache[attributeID] = map
                            }
                            else if (fileID.path.contains("entity_attributes")) {
                                registry.entityTypeData[attributeID] = EntityTypeData(map)
                            }
                            else {
                                logger.warn("for {}: identifier {} will be ignored, as it does not have \"skills\" or \"entities\" in its path!", fileID, attributeID)
                            }
                        }
                    }
                }

                skillCache.forEach { (id, map) ->
                    val data = registry.skillAttributes[id] ?: SkillAttributeData()
                    data.putSkills(map.mapValues { (_, v) -> AttributeSkillData(v) }.toMap())
                }

                registry.apply()

                registry.nextUpdateFlag()
            }
        }

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, newEntity, _, _ -> refreshAttributes(newEntity) }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> refreshAttributes(player) }

        EntityAttributeModifiedEvents.MODIFIED.register(::onHealthModified)
    }
}