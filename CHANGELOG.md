## Additions 💫

### API
- Added `getManager` which decides the proper AttributeConfigManager via a world instance.
- Added `clientManager` & `serverManager` respectively, which are self-explanatory.
## Changes ⚙️
- Cleaned up codebase, adjusting certain logic.
- Updated Endec to match with wispforest's uploaded versions, and resolved issues with reading/writing Endec's that could cause a fatal exception.
- Updated `EntityAttributeSupplier` (again).
## Removals
- Removed `ConfigDefaults`.
- Removed the ability to access config through functions in the API.
- Removed an attribute modified event that changed health. This should be optionally handled in a workspace that implements the API instead.