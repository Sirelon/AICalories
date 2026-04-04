# Replace Icons in Seller Package

## Files to Edit (5 total)

### 1. PreviewAdScreen.kt
- Line 95: `Icons.AutoMirrored.Filled.ArrowForward` → `Res.drawable.ic_arrow_right`
- Line 204: `Icons.Default.ContentCopy` → `Res.drawable.ic_copy`
- Line 228: `Icons.Default.LocalOffer` → `Res.drawable.ic_tag`
- Line 235: `Icons.Default.Description` → `Res.drawable.ic_file_text`
- Line 259: `Icons.Default.AttachMoney` → `Res.drawable.ic_dollar_sign`

Remove imports: ArrowForward, AttachMoney, ContentCopy, Description, LocalOffer
Add import: `org.jetbrains.compose.resources.painterResource`

### 2. AiProcessingContent.kt
- Line 121: `Icons.Rounded.Star` → `Res.drawable.ic_sparkles`
- Line 221: `Icons.Default.Check` → `Res.drawable.ic_check`
- Line 152: `Icons.Default.FlashOn` → **KEEP** (no replacement)

Add import: `org.jetbrains.compose.resources.painterResource`

### 3. GenerateAdScreen.kt
- Line 162: `Icons.Rounded.Star` → `Res.drawable.ic_sparkles`
- Line 370: `Icons.Default.Check` → `Res.drawable.ic_check`
- Line 335: `Icons.Default.FlashOn` → **KEEP** (no replacement)

Add import: `org.jetbrains.compose.resources.painterResource`

### 4. SellerLandingScreen.kt
- Line 116: `Icons.Default.Close` → `Res.drawable.ic_x`
- Line 231: `Icons.Default.Person` → `Res.drawable.ic_user`
- Line 291: `Icons.Default.Person` → `Res.drawable.ic_user`
- Line 321: `Icons.Default.Check` → `Res.drawable.ic_check`

Add import: `org.jetbrains.compose.resources.painterResource`

### 5. OnboardingScreen.kt
- Line 158: `Icons.AutoMirrored.Filled.ArrowBack` → `Res.drawable.ic_arrow_left`
- Line 187: `Icons.AutoMirrored.Filled.ArrowForward` → `Res.drawable.ic_arrow_right`

Remove imports: ArrowBack, ArrowForward
Add import: `org.jetbrains.compose.resources.painterResource`

## Change Pattern

**For `imageVector` parameters:**
```kotlin
// Before
imageVector = Icons.Default.Copy

// After
painter = painterResource(Res.drawable.ic_copy)
```

**For `Icon` composables:**
```kotlin
// Before
Icon(Icons.Default.Copy, contentDescription = "Copy")

// After
Icon(painterResource(Res.drawable.ic_copy), contentDescription = "Copy")
```

## Icon Reference

| Material Icon | SVG Icon |
|---|---|
| ArrowBack | ic_arrow_left |
| ArrowForward | ic_arrow_right |
| AttachMoney | ic_dollar_sign |
| Check | ic_check |
| Close | ic_x |
| ContentCopy | ic_copy |
| Description | ic_file_text |
| LocalOffer | ic_tag |
| Person | ic_user |
| Star | ic_sparkles |
| FlashOn | ❌ KEEP |

All 40 icons already in: `composeApp/src/commonMain/composeResources/drawable/ic_*.svg`
