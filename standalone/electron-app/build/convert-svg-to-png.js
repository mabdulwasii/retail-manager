#!/usr/bin/env node

const sharp = require('sharp');
const path = require('path');
const fs = require('fs');

const svgPath = path.join(__dirname, 'icon-base.svg');
const pngPath = path.join(__dirname, 'icon-1024.png');

console.log('📸 Converting SVG to PNG...');
console.log(`   Input: ${svgPath}`);
console.log(`   Output: ${pngPath}\n`);

const svgBuffer = fs.readFileSync(svgPath);

sharp(svgBuffer)
  .resize(1024, 1024)
  .png()
  .toFile(pngPath)
  .then(() => {
    console.log('✅ PNG created successfully!');
    console.log('   Size: 1024x1024');
    console.log(`   Location: ${pngPath}\n`);
    console.log('💡 Next step: Run node build/generate-icons.js to create all icon formats\n');
  })
  .catch(err => {
    console.error('❌ Error converting SVG to PNG:', err);
    process.exit(1);
  });
