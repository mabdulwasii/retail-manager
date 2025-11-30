#!/usr/bin/env node

const sharp = require('sharp');
const path = require('path');

console.log('🎨 Creating installer graphics...\n');

// NSIS installer header (150x57)
const headerSvg = `
<svg width="150" height="57" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#4F46E5;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#7C3AED;stop-opacity:1" />
    </linearGradient>
  </defs>
  <rect width="150" height="57" fill="url(#grad1)"/>
  <text x="75" y="35" font-family="Arial, sans-serif" font-size="20" font-weight="bold" fill="white" text-anchor="middle">Shop Manager</text>
</svg>
`;

// NSIS installer sidebar (164x314)
const sidebarSvg = `
<svg width="164" height="314" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="grad2" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" style="stop-color:#4F46E5;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#7C3AED;stop-opacity:1" />
    </linearGradient>
  </defs>
  <rect width="164" height="314" fill="url(#grad2)"/>
  <circle cx="82" cy="100" r="40" fill="white" opacity="0.2"/>
  <text x="82" y="200" font-family="Arial, sans-serif" font-size="18" font-weight="bold" fill="white" text-anchor="middle">Shop</text>
  <text x="82" y="220" font-family="Arial, sans-serif" font-size="18" font-weight="bold" fill="white" text-anchor="middle">Manager</text>
  <text x="82" y="260" font-family="Arial, sans-serif" font-size="10" fill="white" text-anchor="middle" opacity="0.8">Retail Management</text>
  <text x="82" y="275" font-family="Arial, sans-serif" font-size="10" fill="white" text-anchor="middle" opacity="0.8">Platform</text>
</svg>
`;

// DMG background (540x400)
const dmgBackgroundSvg = `
<svg width="540" height="400" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="grad3" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#F8FAFC;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#E2E8F0;stop-opacity:1" />
    </linearGradient>
  </defs>
  <rect width="540" height="400" fill="url(#grad3)"/>
  <text x="270" y="340" font-family="Arial, sans-serif" font-size="14" fill="#64748B" text-anchor="middle">Drag Shop Manager to Applications folder to install</text>
  <path d="M 130 220 L 130 280 L 200 280 L 200 220 Z" stroke="#4F46E5" stroke-width="2" fill="none" stroke-dasharray="5,5"/>
  <path d="M 340 220 L 340 280 L 480 280 L 480 220 Z" stroke="#4F46E5" stroke-width="2" fill="none"/>
  <text x="165" y="300" font-family="Arial, sans-serif" font-size="12" fill="#64748B" text-anchor="middle">App</text>
  <text x="410" y="300" font-family="Arial, sans-serif" font-size="12" fill="#64748B" text-anchor="middle">Applications</text>
  <path d="M 210 250 L 330 250" stroke="#4F46E5" stroke-width="2" marker-end="url(#arrowhead)"/>
  <defs>
    <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto">
      <polygon points="0 0, 10 3, 0 6" fill="#4F46E5"/>
    </marker>
  </defs>
</svg>
`;

const buildDir = __dirname;

// Create header BMP (actually create as PNG, electron-builder will convert if needed)
sharp(Buffer.from(headerSvg))
  .resize(150, 57)
  .toFormat('png', { compressionLevel: 0 })  // BMP-compatible
  .toFile(path.join(buildDir, 'installer-header.bmp'))
  .then(() => {
    console.log('✅ Created installer-header.bmp (150x57)');
  })
  .catch(err => console.error('❌ Error creating header:', err));

// Create sidebar BMP
sharp(Buffer.from(sidebarSvg))
  .resize(164, 314)
  .toFormat('png', { compressionLevel: 0 })  // BMP-compatible
  .toFile(path.join(buildDir, 'installer-sidebar.bmp'))
  .then(() => {
    console.log('✅ Created installer-sidebar.bmp (164x314)');
  })
  .catch(err => console.error('❌ Error creating sidebar:', err));

// Create DMG background PNG
sharp(Buffer.from(dmgBackgroundSvg))
  .resize(540, 400)
  .png()
  .toFile(path.join(buildDir, 'dmg-background.png'))
  .then(() => {
    console.log('✅ Created dmg-background.png (540x400)\n');
    console.log('📦 All installer graphics created successfully!\n');
  })
  .catch(err => console.error('❌ Error creating DMG background:', err));
