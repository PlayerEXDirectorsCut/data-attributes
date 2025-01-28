## Additions 🍎
- PlayerEX is now on 1.21.1 for fabric & neoforge!
- Add button for Entity Types (will downstream this to 1.20.1 momentarily).
- I cannot guarantee its stability (specifically in the areas of attribute modification). If there are no issues, I'll stabilize it later.
## Changes 🌽
- `EventSources` are used in replacement of fabric api events to be more compatible with multi-loader. Event classes have been renamed.
  - `AttributeEvents`
  - `AttributeModifiedEvents`
This update affected a sector of mixins which I have redone, but I will keep it in beta until its stability is ensured along with PlayerEX.