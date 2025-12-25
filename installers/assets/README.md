# Shop Manager - Installer Assets

This directory contains assets (icons and images) used by platform-specific installers.

## Required Assets

### Windows

1. **shop-manager.ico** (Required)
   - Multi-resolution ICO file: 16x16, 32x32, 48x48, 256x256
   - Used for: Application icon, shortcuts, taskbar
   - Format: ICO
   - Location: `installers/windows/assets/`

2. **wizard-image.bmp** (Optional)
   - Size: 164x314 pixels
   - Color depth: 8-bit recommended
   - Used for: Left panel of installer wizard
   - Format: BMP
   - Location: `installers/windows/assets/`

3. **wizard-small.bmp** (Optional)
   - Size: 55x58 pixels
   - Color depth: 8-bit recommended
   - Used for: Top-right corner of installer wizard
   - Format: BMP
   - Location: `installers/windows/assets/`

### macOS

1. **shop-manager.icns** (Required)
   - Multi-resolution ICNS file
   - Sizes: 16x16, 32x32, 128x128, 256x256, 512x512, 1024x1024
   - Used for: Application icon, Dock, Finder
   - Format: ICNS
   - Location: `installers/macos/assets/`

2. **dmg-background.png** (Optional)
   - Size: 800x400 pixels recommended
   - Used for: DMG window background
   - Format: PNG
   - Location: `installers/macos/assets/`

### Linux

1. **shop-manager.png** (Required)
   - Size: 256x256 pixels
   - Used for: Desktop entry icon, application menu
   - Format: PNG
   - Location: `installers/linux/assets/`

## Creating Assets from Source

If you have a source PNG image (e.g., `logo.png`), use these scripts to generate all required assets:

### Generate Windows Assets

```bash
# Create ICO from PNG (requires ImageMagick)
convert logo.png -define icon:auto-resize=256,128,96,64,48,32,16 installers/windows/assets/shop-manager.ico

# Create wizard images (using ImageMagick)
convert -size 164x314 -background "#f0f0f0" -fill "#333333" -gravity center -pointsize 48 label:"Shop Manager" installers/windows/assets/wizard-image.bmp

convert -size 55x58 -background "#f0f0f0" -fill "#333333" -gravity center -pointsize 12 label:"SM" installers/windows/assets/wizard-small.bmp
```

**Or use online tools:**
- ICO: https://convertio.co/png-ico/
- BMP: Any image editor (Paint, GIMP, Photoshop)

### Generate macOS Assets

```bash
# Create iconset directory
mkdir -p shop-manager.iconset

# Generate all required sizes (requires sips - macOS built-in)
sips -z 16 16     logo.png --out shop-manager.iconset/icon_16x16.png
sips -z 32 32     logo.png --out shop-manager.iconset/icon_16x16@2x.png
sips -z 32 32     logo.png --out shop-manager.iconset/icon_32x32.png
sips -z 64 64     logo.png --out shop-manager.iconset/icon_32x32@2x.png
sips -z 128 128   logo.png --out shop-manager.iconset/icon_128x128.png
sips -z 256 256   logo.png --out shop-manager.iconset/icon_128x128@2x.png
sips -z 256 256   logo.png --out shop-manager.iconset/icon_256x256.png
sips -z 512 512   logo.png --out shop-manager.iconset/icon_256x256@2x.png
sips -z 512 512   logo.png --out shop-manager.iconset/icon_512x512.png
sips -z 1024 1024 logo.png --out shop-manager.iconset/icon_512x512@2x.png

# Convert to ICNS (macOS built-in)
iconutil -c icns shop-manager.iconset -o installers/macos/assets/shop-manager.icns

# Cleanup
rm -rf shop-manager.iconset

# Create DMG background (using ImageMagick)
convert -size 800x400 -background "#f0f0f0" -fill "#333333" -gravity center -pointsize 48 label:"Shop Manager" installers/macos/assets/dmg-background.png
```

**Or use online tools:**
- ICNS: https://cloudconvert.com/png-to-icns
- PNG: Any image editor

### Generate Linux Assets

```bash
# Resize to 256x256 (using ImageMagick)
convert logo.png -resize 256x256 installers/linux/assets/shop-manager.png

# Or using sips (macOS)
sips -z 256 256 logo.png --out installers/linux/assets/shop-manager.png
```

## Placeholder Assets

If you don't have custom assets, the build scripts will work without them, but will show warnings. To create basic placeholders:

### Quick Placeholder Script (All Platforms)

```bash
#!/bin/bash
# Creates basic placeholder assets

# Windows
mkdir -p installers/windows/assets
# Note: ICO creation requires ImageMagick
# For now, use online converter or skip - installer will work without it

# macOS
mkdir -p installers/macos/assets
# Note: ICNS creation requires macOS iconutil
# For now, use online converter or skip - will use default icon

# Linux
mkdir -p installers/linux/assets
convert -size 256x256 -background "#4A90E2" -fill white -gravity center \
    -font Arial -pointsize 72 label:"SM" \
    installers/linux/assets/shop-manager.png
```

## Asset Specifications

### Icon Design Guidelines

1. **Simplicity**: Icons should be simple and recognizable at small sizes (16x16)
2. **Contrast**: Use good contrast for visibility on light and dark backgrounds
3. **Consistency**: Use the same visual style across all platforms
4. **Transparency**: Include alpha channel for rounded corners and shadows

### Recommended Tools

**Free:**
- GIMP (https://www.gimp.org/) - All platforms
- Inkscape (https://inkscape.org/) - Vector graphics
- ImageMagick (https://imagemagick.org/) - Command-line processing

**Online:**
- https://convertio.co/ - Format conversion
- https://cloudconvert.com/ - Format conversion
- https://favicon.io/ - Simple icon generator

**Commercial:**
- Adobe Photoshop - Professional icon design
- Sketch (macOS) - UI/Icon design
- Figma - Collaborative design

## Verification

After creating assets, verify them:

### Windows
```powershell
# Check ICO file
Get-ItemProperty installers\windows\assets\shop-manager.ico | Select-Object Name, Length
```

### macOS
```bash
# Check ICNS file
file installers/macos/assets/shop-manager.icns
# Should output: "...Apple icon image"

# Preview ICNS
qlmanage -p installers/macos/assets/shop-manager.icns
```

### Linux
```bash
# Check PNG file
file installers/linux/assets/shop-manager.png
# Should output: "PNG image data, 256 x 256..."

# View dimensions
identify installers/linux/assets/shop-manager.png
```

## Troubleshooting

### "ImageMagick not found"

**Windows:**
```powershell
choco install imagemagick
```

**macOS:**
```bash
brew install imagemagick
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt-get install imagemagick

# RHEL/Fedora
sudo yum install ImageMagick
```

### "Icon not displaying in installer"

**Windows:**
- Ensure ICO file has multiple resolutions (16, 32, 48, 256)
- Verify file path in `shop-manager.iss` is correct
- Rebuild installer after adding icon

**macOS:**
- Ensure ICNS file is valid: `file shop-manager.icns`
- Clear icon cache: `sudo rm -rf /Library/Caches/com.apple.iconservices.store`
- Rebuild DMG after adding icon

**Linux:**
- Ensure PNG is exactly 256x256: `identify shop-manager.png`
- Verify file permissions: `chmod 644 shop-manager.png`
- Update desktop database: `sudo update-desktop-database`

## License

All assets should be created or licensed appropriately for commercial use. Ensure you have rights to any logos, images, or designs used.

---

**Last Updated**: 2025-12-24
