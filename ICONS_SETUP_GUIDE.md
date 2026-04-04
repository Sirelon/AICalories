# AICalories Icons Setup Guide

## Overview
This guide explains how to integrate the 39 newly created Lucide SVG icons into your Compose Multiplatform project.

## Files Created

### SVG Icon Files (39 total)
All SVG files are located in the `drawable/` folder:
- **Location**: `drawable/*.svg`
- **Format**: Standard SVG with viewBox="0 0 24 24"
- **Icons**: ArrowLeft, Baby, BookOpen, Camera, Car, Check, ChevronLeft, ChevronRight, CircleAlert, CircleCheckBig, Copy, DollarSign, Dumbbell, Eye, FileText, Frown, Gift, Heart, Home, LayoutGrid, Meh, Palette, PenLine, RefreshCw, Server, Share2, Shirt, Smartphone, Sparkles, Tag, TreePine, TrendingUp, TriangleAlert, Upload, User, WandSparkles, WifiOff, Wrench, X

### Compose Preview Screen
- **File**: `IconPreviewScreen.kt`
- **Purpose**: Kotlin Compose composable that displays all icons in a grid
- **Dependencies**: Uses Compose's resource system to load icons

### HTML Preview
- **File**: `icon_preview.html`
- **Purpose**: Visual preview of all icons (open in browser)
- **Features**:
  - Click any icon to copy the name
  - Responsive grid layout
  - Shows all 39 icons with labels

## Integration Steps

### Step 1: Copy SVG Files to Your Project
Copy all SVG files from the `drawable/` folder to your project's drawable resources:

```bash
cp drawable/*.svg /path/to/AICalories/composeApp/src/commonMain/composeResources/drawable/
```

The destination path is:
```
composeApp/src/commonMain/composeResources/drawable/
```

### Step 2: Regenerate Compose Resources
After adding the SVG files, Compose will automatically regenerate the resource accessors:

1. Perform a clean build:
   ```bash
   ./gradlew clean build
   ```

2. Or use your IDE's build function to regenerate resources

This will create the resource references in:
```
composeApp/build/generated/compose/resourceGenerator/.../Res
```

### Step 3: Add Preview Screen (Optional)
If you want to verify all icons are working:

1. Copy `IconPreviewScreen.kt` to your designsystem/screens package:
   ```
   composeApp/src/commonMain/kotlin/com/sirelon/aicalories/designsystem/screens/
   ```

2. Update the imports in the file to match your project structure

3. Add it to your navigation or create a debug screen to test

### Step 4: Update Navigation (Optional)
If you want to add the icon preview as a route:

1. Add to `AppDestination`:
   ```kotlin
   @Serializable
   data object IconPreview : AppDestination
   ```

2. Add to the App.kt entry provider:
   ```kotlin
   entry<AppDestination.IconPreview> {
       IconPreviewScreen()
   }
   ```

## Using Icons in Your App

Once integrated, use the icons anywhere in Compose:

```kotlin
import androidx.compose.material3.Icon
import com.sirelon.aicalories.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource

Icon(
    painter = painterResource(Res.drawable.ic_heart),
    contentDescription = "Favorite",
    tint = Color.Red,
    modifier = Modifier.size(24.dp)
)
```

## Icon Specifications

- **ViewBox**: 0 0 24 24
- **Default Size**: 24x24 dp
- **Stroke Width**: 2
- **Style**: Outline (some icons have fill elements)
- **Color**: Uses `currentColor` (respects tint parameter)

## Verification

### Visual Check
Open `icon_preview.html` in your browser to visually verify all icons are correct.

### In-App Check
Use the `IconPreviewScreen` to verify icons load correctly in your Compose app.

## Available Icons (39 total)

```
ArrowLeft         Baby              BookOpen          Camera
Car               Check             ChevronLeft       ChevronRight
CircleAlert       CircleCheckBig    Copy              DollarSign
Dumbbell          Eye               FileText          Frown
Gift              Heart             Home              LayoutGrid
Meh               Palette           PenLine           RefreshCw
Server            Share2            Shirt             Smartphone
Sparkles          Tag               TreePine          TrendingUp
TriangleAlert     Upload            User              WandSparkles
WifiOff           Wrench            X
```

## Troubleshooting

### Icons not showing up?
1. Check that SVG files are in the correct directory
2. Run `./gradlew clean build` to regenerate resources
3. Clear IDE cache and restart

### Build errors about missing resources?
1. Ensure all SVG file names match the drawable references exactly
2. Check that Compose Multiplatform version supports drawable resources
3. Run resource generation manually: `./gradlew :composeApp:generateResourcesKmp`

### Icon looks different than expected?
1. Check the SVG file directly to ensure it was copied correctly
2. Verify the tint color is set correctly
3. Try adjusting the size modifier

## Next Steps

1. **Copy SVG files** to your project's drawable folder
2. **Rebuild** the project
3. **Test** by opening the preview screen or HTML file
4. **Integrate** icons into your app components as needed

## Notes

- All icons are from the Lucide icon set (ISC License)
- SVGs are optimized for Compose with proper stroke settings
- Icons are scalable and support color customization via tint
- No external dependencies required beyond Compose resources
