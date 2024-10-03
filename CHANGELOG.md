*Welcome to the release of the new Data Attributes. This has taken a considerable amount of time to complete.*

*I am glad to have this opportunity to work on this project and be supported with the first ever public project I will personally release.*

## Additions 🍎
- Allowed easier control of the config menu, and added some new features.
  - A `Reset` option to reset your targeted attribute, refreshing all its entries to start anew.
  - A `Remove` option to remove the targeted entry of your choice.
  - A `Edit` option to edit the identifier to target a different entry.
  - A `Add` option to include new entries.
- Attributes in configuration now will re-render in certain scenarios, allowing for a better experience with working with multiple attributes.
- You can now actually use the search bar to look up the specific entries you wish to find.
  - Translations should be compatible in the language you choose as well as the attribute id.
    - e.g., looking up `playerex:luck`, or `Luck` should work.

## Changes 🌽
- Made some changes to certain logic internally and micro-optimizations.
- Fixed CTD issues with editing function values.
- Separated config entries from defaults using color coding & tooltips.
- Resolved issue with diminishing returns. Hopefully, this should work.