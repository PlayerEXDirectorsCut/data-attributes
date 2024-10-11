## Overview
**There are breaking changes in this release. Ensure you back up your config(s) if possible. Mods relying on the beta version will have to be updated.**

*Welcome to the release of the new Data Attributes. This has taken a considerable amount of time to complete.*

*I am glad to have this opportunity to work on this project and be supported with the first ever public project I will personally release.*

## Additions 🍎
- No more needing to leave the UI to your config json~ you can do everything you need to do in the UI!
- Allowed easier control of the config menu, and added some new features.
  - A `Reset` option to reset your targeted attribute, refreshing all its entries to start anew.
  - A `Remove` option to remove the targeted entry of your choice.
  - A `Edit` option to edit the identifier to target a different entry.
  - A `Add` option to include new entries.
  - You can also add into default entries.
- Added autocomplete that will appear while editing fields. This will show you currently registered entities and attributes when editing the respective id you want.
  - Press [ENTER] to quickly grab the first entry on the autocomplete.
- Attribute components in configuration now will re-render in certain scenarios, allowing for a better experience with working with multiple attributes.
- Improved logic with entering fields, commit changes by pressing [Done], and [Reload] to refresh to the latest config.
- You can now actually use the search bar to look up the specific entries you wish to find.
  - Translations should be compatible in the language you choose as well as the attribute id.
    - e.g., looking up `playerex:luck`, or `Luck` should work.
- You can now enable/disable Attribute Functions.

## Changes 🌽
- **[BREAKING]** Changed mockup of config JSON.
  - Config keys such as `"functions"/"overrides"/"entity_types": { ... }` have been replaced with `"entries": { ... }`.
  - This retains parity with the data-pack format.
- **[BREAKING]** Changed `Map<Identifier, Double>` to `Map<Identifier, EntityTypeEntry>` for `EntityTypeData`.
  - This also includes a `fallback` value that gets the default registered base value for the specific attribute under that entity.
- **[BREAKING]** Changed overall structure of config related class definitions. This will affect your config file considerably.
- **[BREAKING]** Removed `DefaultAttributeFactory`.
- **[BREAKING]** Changed `Map<Identifier, List<AttributeFunction>>` to `Map<Identifier, Map<Identifier, AttributeFunction>>`
  - This existed to avoid an odd situation that does not exist anymore.
- Made some changes to certain logic internally and micro-optimizations.
- Fixed CTD issues with editing function values.
- Separated config entries from defaults using color coding & tooltips.
- **[BREAKING]** Integrated diminishing returns as intended.
  - You must keep and/or target attribute min/max ranges between -1 & 1.
  - It is how it usually worked in the original DataAttributes, as unfortunately, a solution to implement diminishing returns on all attributes was not possible (at this time).