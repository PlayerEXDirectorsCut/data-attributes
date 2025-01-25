package com.bms.data_attributes.ui.components

import com.bms.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component

open class CollapsibleFoldableContainer(horizontalSizing: Sizing, verticalSizing: Sizing, title: Component, expanded: Boolean) : CollapsibleContainer(horizontalSizing, verticalSizing, title, expanded) {
    var isFoldered = !this.expanded

    fun getFoldableText(): MutableComponent {
        return if (isFoldered) {
            Component.translatable("ui.data_attributes.cfc.uncollapse_all")
        }
        else {
            Component.translatable("ui.data_attributes.cfc.collapse_all")
        }
    }

    override fun toggleExpansion() {
        super.toggleExpansion()

        // set the fold button to be visible or invisible based on collapsed state.
        this.childById(ButtonComponent::class.java, "collapsible_foldable.button")?.let {
            it.active = this.expanded()
        }
    }

    init {
        Components.button(this.getFoldableText()) {
            this.forEachDescendant { descendant ->
                if (descendant == this || descendant !is CollapsibleContainer) return@forEachDescendant
                if (isFoldered && descendant.expanded() || !isFoldered && !descendant.expanded()) return@forEachDescendant
                descendant.toggleExpansion()
            }
            isFoldered = !isFoldered
            it.message = this.getFoldableText()
        }
            .also { this.titleLayout().child(2, it) }
            .renderer(ButtonRenderers.STANDARD)
            .zIndex(10)
            .verticalSizing(Sizing.fixed(10))
            .horizontalSizing(Sizing.content())
            .id("collapsible_foldable.button")
    }
}