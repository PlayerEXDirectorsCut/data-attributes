package com.bibireden.data_attributes.ui.components

import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.MutableText
import net.minecraft.text.Text

class CollapsibleFoldableContainer(horizontalSizing: Sizing, verticalSizing: Sizing, title: Text, expanded: Boolean) : CollapsibleContainer(horizontalSizing, verticalSizing, title, expanded) {
    var isFoldered = !expanded()

    fun getFoldableText(): MutableText {
        return if (isFoldered) {
            Text.translatable("ui.data_attributes.collapsiblefoldablecontainer.uncollapse_all")
        }
        else {
            Text.translatable("ui.data_attributes.collapsiblefoldablecontainer.collapse_all")
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
            .verticalSizing(Sizing.fixed(10))
            .id("collapsible_foldable.button")
    }
}