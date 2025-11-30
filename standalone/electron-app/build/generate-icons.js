#!/usr/bin/env node

/**
 * Icon Generation Script for Shop Manager Desktop
 *
 * Generates all required icon files for Windows, macOS, and Linux from a base PNG
 *
 * Usage: node build/generate-icons.js
 */

const iconMaker = require('electron-icon-maker');
const path = require('path');
const fs = require('fs');

const options = {
  input: path.join(__dirname, 'icon-1024.png'),
  output: path.join(__dirname),
  modes: {
    ico: {
      name: 'icon',
      sizes: [16, 24, 32, 48, 64, 128, 256]
    },
    icns: {
      name: 'icon',
      sizes: [16, 32, 64, 128, 256, 512, 1024]
    },
    favicon: false,
    linux: true
  }
};

console.log('🎨 Generating icon files for all platforms...\n');

// Check if base icon exists
if (!fs.existsSync(options.input)) {
  console.error('❌ Error: Base icon not found at', options.input);
  console.error('\n📝 To generate icons:');
  console.error('   1. Create a 1024x1024 PNG icon named "icon-1024.png" in the build/ directory');
  console.error('   2. Run this script again: node build/generate-icons.js');
  console.error('\n💡 You can convert the SVG to PNG using:');
  console.error('   - Online tools: https://convertio.co/svg-png/');
  console.error('   - ImageMagick: convert -density 1200 -resize 1024x1024 icon-base.svg icon-1024.png');
  console.error('   - Inkscape: inkscape -w 1024 -h 1024 icon-base.svg -o icon-1024.png\n');
  process.exit(1);
}

iconMaker(options)
  .then(() => {
    console.log('✅ Icon generation complete!\n');
    console.log('📦 Generated files:');
    console.log('   • build/icon.icns (macOS)');
    console.log('   • build/icon.ico (Windows)');
    console.log('   • build/icons/*.png (Linux)\n');

    // Copy icon.ico to installer icons if they don't exist
    const icoPath = path.join(__dirname, 'icon.ico');
    const installerIconPath = path.join(__dirname, 'installer-icon.ico');
    const uninstallerIconPath = path.join(__dirname, 'uninstaller-icon.ico');

    if (!fs.existsSync(installerIconPath)) {
      fs.copyFileSync(icoPath, installerIconPath);
      console.log('   • Copied to installer-icon.ico');
    }

    if (!fs.existsSync(uninstallerIconPath)) {
      fs.copyFileSync(icoPath, uninstallerIconPath);
      console.log('   • Copied to uninstaller-icon.ico\n');
    }
  })
  .catch(err => {
    console.error('❌ Error generating icons:', err);
    process.exit(1);
  });
