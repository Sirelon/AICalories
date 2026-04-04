# Seller Package Icon Replacements Guide

This guide shows exactly how to replace Material Design Icons with SVG drawable icons in the seller package.

## Files to Modify

### 1. PreviewAdScreen.kt
**Location:** `composeApp/src/commonMain/kotlin/com/sirelon/aicalories/features/seller/ad/preview_ad/PreviewAdScreen.kt`

**Step 1: Update Imports**
Remove these lines (22-27):
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalOffer
```

Add this import (after the Icon import):
```kotlin
import org.jetbrains.compose.resources.painterResource
```

**Step 2: Replace Icon References**

Line 95 - Change:
```kotlin
trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
```
To:
```kotlin
trailingIcon = null, // Will add painter separately
```
Then update the AppButton composable to use painter parameter if available, or wrap Icon:
```kotlin
Icon(
    painter = painterResource(Res.drawable.ic_arrow_right),
    contentDescription = "Publish",
    modifier = Modifier.size(24.dp)
)
```

Line 204 - Change:
```kotlin
Icon(
    Icons.Default.ContentCopy,
    contentDescription = "Copy"
)
```
To:
```kotlin
Icon(
    painter = painterResource(Res.drawable.ic_copy),
    contentDescription = "Copy"
)
```

Line 228 - Change:
```kotlin
PreviewSectionCard(icon = Icons.Default.LocalOffer, ...
```
To:
```kotlin
PreviewSectionCard(icon = null, painter = painterResource(Res.drawable.ic_tag), ...
```
(Note: You may need to update PreviewSectionCard signature to accept painter)

Line 235 - Change:
```kotlin
icon = Icons.Default.Description,
```
To:
```kotlin
icon = null, painter = painterResource(Res.drawable.ic_file_text),
```

Line 259 - Change:
```kotlin
icon = Icons.Default.AttachMoney,
```
To:
```kotlin
icon = null, painter = painterResource(Res.drawable.ic_dollar_sign),
```

---

### 2. AiProcessingContent.kt
**Location:** `composeApp/src/commonMain/kotlin/com/sirelon/aicalories/features/seller/ad/generate_ad/AiProcessingContent.kt`

**Step 1: Add Import**
Add:
```kotlin
import org.jetbrains.compose.resources.painterResource
```

**Step 2: Replace Icons**

Line 121 - Change:
```kotlin
imageVector = Icons.Rounded.Star,
```
To:
```kotlin
painter = painterResource(Res.drawable.ic_sparkles),
```
(Also change parameter name from `imageVector` to `painter`)

Line 221 - Change:
```kotlin
imageVector = Icons.Default.Check,
```
To:
```kotlin
painter = painterResource(Res.drawable.ic_check),
```

Keep line 152 as-is (FlashOn icon - no replacement available):
```kotlin
imageVector = Icons.Default.FlashOn,  // No SVG replacement
```

---

### 3. GenerateAdScreen.kt
**Location:** `composeApp/src/commonMain/kotlin/com/sirelon/aicalories/features/seller/ad/generate_ad/GenerateAdScreen.kt`

**Step 1: Add Import** (if not already present)
```kotlin
import org.jetbrains.compose.resources.painterResource
```

**Step 2: Replace Icons**

Line 162 - Change:
```kotlin
leadingIcon = if (state.isLoading) null else Icons.Rounded.Star,
```
To:
```kotlin
leadingIcon = null,  // Using painter instead
```
Then use painter in the button content or find alternative.

Line 370 - Change:
```kotlin
imageVector = Icons.Default.Check,
```
To:
```kotlin
painter = painterResource(Res.drawable.ic_check),
```

Keep line 335 as-is:
```kotlin
imageVector = Icons.Default.FlashOn,  // No SVG replacement
```

---

### 4. SellerLandingScreen.kt
**Location:** `composeApp/src/commonMain/kotlin/com/sirelon/aicalories/features/seller/auth/presentation/SellerLandingScreen.kt`

**Step 1: Add Import** (if not already present)
```kotlin
import org.jetbrains.compose.resources.painterResource
```

**Step 2: Replace Icons**

Line 116 - Change:
```kotlin
Icon(Icons.Default.Close, contentDescription = "Close")
```
To:
```kotlin
Icon(painterResource(Res.drawable.ic_x), contentDescription = "Close")
```

Line 231 - Change:
```kotlin
leadingIcon = Icons.Default.Person
```
To:
```kotlin
leadingIcon = null,  // Using painter in content
```
And add Icon with painter.

Line 291 - Change:
```kotlin
imageVector = Icons.Default.Person,
```
To:
```kotlin
painter = painterResource(Res.drawable.ic_user),
```

Line 321 - Change:
```kotlin
imageVector = Icons.Default.Check,
```
To:
```kotlin
painter = painterResource(Res.drawable.ic_check),
```

---

### 5. OnboardingScreen.kt
**Location:** `composeApp/src/commonMain/kotlin/com/sirelon/aicalories/features/seller/onboarding/OnboardingScreen.kt`

**Step 1: Update Imports**
Remove:
```kotlin
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
```

Add:
```kotlin
import org.jetbrains.compose.resources.painterResource
```

**Step 2: Replace Icons**

Line 158 - Change:
```kotlin
icon = Icons.AutoMirrored.Filled.ArrowBack,
```
To:
```kotlin
icon = null,  // Using painter
```
Then add painter with ic_arrow_left.

Line 187 - Change:
```kotlin
trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
```
To:
```kotlin
trailingIcon = null,  // Using painter
```
Then add Icon with ic_arrow_right painter.

---

## Icon Mapping Reference

| Old Material Icon | New SVG Icon | File Name |
|---|---|---|
| Icons.Default.ArrowBack | ic_arrow_left | ic_arrow_left.svg |
| Icons.Default.ArrowForward* | ic_arrow_right | ic_arrow_right.svg |
| Icons.Default.AttachMoney | ic_dollar_sign | ic_dollar_sign.svg |
| Icons.Default.Check | ic_check | ic_check.svg |
| Icons.Default.Close | ic_x | ic_x.svg |
| Icons.Default.ContentCopy | ic_copy | ic_copy.svg |
| Icons.Default.Description | ic_file_text | ic_file_text.svg |
| Icons.Default.FlashOn | ❌ NOT AVAILABLE | Keep Material Icon |
| Icons.Default.LocalOffer | ic_tag | ic_tag.svg |
| Icons.Default.Person | ic_user | ic_user.svg |
| Icons.Rounded.Star | ic_sparkles | ic_sparkles.svg |
| Icons.AutoMirrored.Filled.ArrowForward | ic_arrow_right | ic_arrow_right.svg |

*ArrowForward is a Compose Material icon that points right; use ic_arrow_right

---

## Notes

- FlashOn icon (lightning bolt) has no equivalent in our SVG set - keep the Material icon
- When changing from `imageVector` to `painter`, make sure the parameter name changes too
- Ensure `painterResource` is imported in each file
- Some composables may have custom parameters - adjust according to your implementation
- Test each screen after making changes to ensure icons display correctly
