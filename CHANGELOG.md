## Additions 🍎
- Implemented a feature that reads data-packs and applies attribute modifications to the world based on order.
  - More documentation on this feature will come as the mod stabilizes.
  - For now, if you want to be aware of how to format your data-pack(s), look towards the source code of the next builds of PlayerEX/WizardEX.
## Changes 🌽
- Changed how configs and modded entries are applied.
  - Your config will appear sparse on startup, but when you go to a world (in an integrated server), it will show the extra modifications provided by loaded packs and mods.
  - You can overwrite these, and they will appear in your actual config. Your config will **overwrite** any data-pack or modded pack.
  - The UI will be overhauled to better assist in knowing what is supplied by datapacks/mods, but that will be in the later future.
- Deprecated the `DefaultAttributeRegistry` to keep previous versions functional, but it will be removed before release.