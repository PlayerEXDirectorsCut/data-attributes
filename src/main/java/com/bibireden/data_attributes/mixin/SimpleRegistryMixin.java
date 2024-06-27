package com.bibireden.data_attributes.mixin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bibireden.data_attributes.mutable.MutableSimpleRegistry;
import com.mojang.serialization.Lifecycle;

import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;

@Mixin(SimpleRegistry.class)
abstract class SimpleRegistryMixin<T> implements MutableSimpleRegistry<T> {
    @Unique
    private Collection<Identifier> data_idCache;

    @Final
    @Shadow
    private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Final
    @Shadow
    private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

    @Final
    @Shadow
    private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Final
    @Shadow
    private Map<T, Lifecycle> entryToLifecycle;

    @Shadow
    private Lifecycle lifecycle;

    @Shadow
    private List<RegistryEntry.Reference<T>> cachedEntries;

    @Shadow
    private int nextId;

    @Shadow
    private boolean frozen;

    @Shadow
    @Final
    private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow
    @Final
    private Object2IntMap<T> entryToRawId;

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void data_attributes$init(CallbackInfo ci) {
        this.data_idCache = new HashSet<>();
    }

    @Inject(method = "freeze", at = @At("RETURN"))
    private void freeze(CallbackInfoReturnable<T> cir) { this.frozen = false; }

    @Unique
    private <V extends T> void remove(RegistryKey<T> key, Lifecycle lifecycle) {
        Validate.notNull(key);
        RegistryEntry.Reference<T> reference = this.keyToEntry.get(key);
        T value = reference.value();
        final int rawId = this.entryToRawId.getInt(value);

        this.rawIdToEntry.remove(rawId);
        this.valueToEntry.remove(value);
        this.idToEntry.remove(key.getValue());
        this.keyToEntry.remove(key);
        this.nextId--;
        this.lifecycle = this.lifecycle.add(lifecycle);
        this.entryToLifecycle.remove(value);
        this.cachedEntries = null;
        this.entryToRawId.remove(value);

        for (T t : this.entryToRawId.keySet()) {
            int i = this.entryToRawId.get(t);
            if (i > rawId) {
                this.entryToRawId.replace(t, i - 1);
            }
        }
    }

    @Override
    public void data_attributes$removeCachedIds(Registry<T> registry) {
        for (Iterator<Identifier> iterator = this.data_idCache.iterator(); iterator.hasNext();) {
            Identifier id = iterator.next();
            this.remove(RegistryKey.of(registry.getKey(), id), Lifecycle.stable());
            iterator.remove();
        }
    }

    @Override
    public void data_attributes$cacheID(Identifier id) {
        this.data_idCache.add(id);
    }
}
