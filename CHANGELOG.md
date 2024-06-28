# Changelog

## Alpha Notes
**SMOOTHNESS DOES NOT WORK YET!**

**If you want to add in a new entry in general, you will need to manually edit the config.**

**Diminishing factor may be inaccurate.**

**Multiplication & Addition of attribute functions are guaranteed to work.**

*alpha, so we aren't formalizing this entirely yet. once beta releases this changelog will be organized better.*

## Additions 💫
- Added `owo-lib`, which includes the following with included purpose:
    - A UI screen that is accessible with the `/owo-config data_attributes` command (it looks pretty nice).
    - A config that syncs its data upon being saved, and then reloaded through the `/reload` command.
    - Implemented new ways to apply your attribute changes to your world.
- There is safety when setting up your configs. If you make a mistake, or if you put in an entry of an attribute that does not exist, you are able to visually see what the issue(s) are. Currently, these are:
  - Non-existing entities/attributes
  - Invalid number values (todo).
- The addition of **smoothness** value (todo)
- The addition of **Fallback Minimum/Maximum** values, which are **extracted** from the attribute of choice and automatically loaded into your config and UI once you add an entry and reload/open the config.
    - *This will give you better insight on what values you should set for the specific attribute, or if you wish to go out of bounds from it.*
- Button to disable each override.
  - When an override is **disabled**, everything about the override falls back to vanilla/other-modded behavior upon reload.
- Tooltip(s) for discovered attributes.
  - When an attribute is discovered and sub-sequentially hovered (hah! that rhymes), a tooltip will appear showing the currently set **minimum & maximum** values for the attribute, to assist in setting up functions or entity-type values.
## Removals 🚫
- Erasure of the existence of a mock attribute that gets created if there is none statically registered.
    - This means that any attribute that you override **MUST** already be in the attribute registry (this may be subject to change in the future, with a much safer solution).
- Entire removal of the data-pack system in favor of a set of configuration files. If you are looking for your changes to replicate to the world and others, use the `/reload` command after you save your config.
- Removal of odd behavior of decreasing entity health based on the previous value if maximum health was changed. Instead, it is **clamped** to the maximum health.
  - If you wish for this behavior again, you can still implement it using the `EntityAttributeModifiedEvents#MODIFIED` event.
## Changes
- Implemented `fabric-language-kotlin`, a required dependency, as DataAttributes is now built with Kotlin.
- Changed networking behavior
- Changed entire mixin internals and mod compatibility
  - `SimpleRegistryMixin` is no more! We have broken our chains from fundamentally re-registering the `ATTRIBUTE` registry (there is no need to as we do not create our own attributes dynamically anymore).
- Changed a huge amount of internals with the mod itself, allowing it to be more compatible with other mods.
    - Falls back to vanilla behavior on most, if not all mixins depending on circumstances, such as an override being disabled, or modifiers depending on the case.