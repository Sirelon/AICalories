# Seller Package Icon Replacements - Summary

## Status: ✅ Ready for Implementation

All 40 SVG icons (including new arrow_right) have been copied to the project and are ready to use.

## What Was Done

1. ✅ Created 39 Lucide SVG icons with `ic_[name]` naming convention
2. ✅ Created `ic_arrow_right.svg` (new icon needed for seller package)
3. ✅ Copied all 40 icons to `composeApp/src/commonMain/composeResources/drawable/`
4. ✅ Created detailed replacement guide with line-by-line instructions

## What Needs to be Done

Replace Material Design Icons in 5 files in the seller package with new SVG icons.

### Files to Update:
1. **PreviewAdScreen.kt** - 5 icon replacements
2. **AiProcessingContent.kt** - 2 icon replacements (FlashOn cannot be replaced)
3. **GenerateAdScreen.kt** - 2 icon replacements (FlashOn cannot be replaced)
4. **SellerLandingScreen.kt** - 4 icon replacements
5. **OnboardingScreen.kt** - 2 icon replacements

**Total Replacements: 15 icons + 1 icon to keep (FlashOn)**

## Icons Available for Seller Package

### Directly Replaceable:
- ✅ ic_arrow_left.svg
- ✅ ic_arrow_right.svg (NEW)
- ✅ ic_check.svg
- ✅ ic_copy.svg
- ✅ ic_dollar_sign.svg
- ✅ ic_file_text.svg
- ✅ ic_sparkles.svg
- ✅ ic_tag.svg
- ✅ ic_user.svg
- ✅ ic_x.svg

### Cannot Replace (Missing):
- ❌ ic_flash_on.svg (Lightning bolt icon - used in AiProcessingContent & GenerateAdScreen)

## How to Apply Changes

See `SELLER_ICON_REPLACEMENTS.md` for detailed line-by-line instructions for each file.

### Quick Summary of Changes:

**All files need:**
1. Add: `import org.jetbrains.compose.resources.painterResource`
2. Remove: Material Icons imports for the icons being replaced
3. Replace: `Icons.Default.X` → `painterResource(Res.drawable.ic_x)`
4. Replace: `imageVector =` → `painter =` (where using painterResource)

## Files Location

All SVG icon files are ready at:
```
composeApp/src/commonMain/composeResources/drawable/ic_*.svg
```

## Next Steps

1. Review `SELLER_ICON_REPLACEMENTS.md` for detailed instructions
2. Apply changes to each of the 5 files
3. Build and test the seller package screens
4. Commit the changes

## Testing Checklist

- [ ] PreviewAdScreen shows all icons correctly
- [ ] AiProcessingContent shows spinner and success icons
- [ ] GenerateAdScreen displays properly
- [ ] SellerLandingScreen shows all account icons
- [ ] OnboardingScreen navigation arrows work
- [ ] FlashOn icon (lightning) still displays (Material Design)
- [ ] No broken references or missing imports

## Notes

- All 40 SVG icons follow the `ic_[name]` naming convention
- Icons are scalable and support color tinting via `tint` parameter
- Arrow icons are directional - verify correct usage (arrow_left vs arrow_right)
- The FlashOn icon is intentionally kept as Material Design since no SVG equivalent exists
