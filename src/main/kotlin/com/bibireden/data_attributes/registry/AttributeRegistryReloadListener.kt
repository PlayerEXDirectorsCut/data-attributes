package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.DataAttributes
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

/** Workaround listener required for*/
class AttributeRegistryReloadListener : SimpleSynchronousResourceReloadListener {

    override fun reload(manager: ResourceManager) {
        DataAttributes.logger.info("ReloadDynamicRegistry :: ResourceManager#:{}", manager)
    }

    override fun getFabricId(): Identifier {
        TODO("Not yet implemented")
    }


}