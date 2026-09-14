const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const htmlPath = 'C:\\Users\\Siku\\Downloads\\stitch_custom_keyboard_key_themes\\code.html';
const outDir = 'C:\\Users\\Siku\\.gemini\\antigravity\\scratch\\Panda Keyboard\\extracted_icons';
const edgePath = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';

if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
}

const htmlContent = fs.readFileSync(htmlPath, 'utf8');

const themeList = [
    { num: 11, name: 'panda' },
    { num: 12, name: 'pig' },
    { num: 13, name: 'flower' },
    { num: 14, name: 'fire' },
    { num: 15, name: 'earth' },
    { num: 16, name: 'butterfly' },
    { num: 17, name: 'football' },
    { num: 18, name: 'basketball' },
    { num: 19, name: 'gift' },
    { num: 20, name: 'pad' },
    { num: 21, name: 'pizzap' }
];

themeList.forEach(({ num, name }) => {
    const sectionRegex = new RegExp(`<section id="set-${num}"[\\s\\S]*?<\\/section>`, 'i');
    const sectionMatch = htmlContent.match(sectionRegex);
    if (!sectionMatch) {
        console.error(`Section set-${num} not found!`);
        return;
    }
    const sectionStr = sectionMatch[0];

    // Match 7:1 Search Bar div
    const match71 = sectionStr.match(/(<div class="[^"]*aspect-7-1[\s\S]*?<\/div>)\s*<\/div>/i);
    // Match 2:1 Special Key div
    const match21 = sectionStr.match(/(<div class="[^"]*aspect-2-1[\s\S]*?<\/div>)\s*<\/div>/i);
    // Match 1:1 Emoji Key div
    const match11 = sectionStr.match(/(<div class="[^"]*aspect-1-1[\s\S]*?<\/div>)\s*<\/div>/i);

    const assetItems = [
        { type: '7-1_space', match: match71, width: 700, height: 100 },
        { type: '2-1_special', match: match21, width: 400, height: 200 },
        { type: '1-1_mascot', match: match11, width: 300, height: 300 }
    ];

    assetItems.forEach(({ type, match, width, height }) => {
        const pngFile = path.join(outDir, `set_${num}_${name}_${type}.png`);
        if (fs.existsSync(pngFile) && fs.statSync(pngFile).size > 1000) {
            console.log(`- Skipping set_${num}_${name}_${type}.png (Already exists)`);
            return;
        }

        if (!match || !match[1]) {
            console.error(`Missing match for set_${num}_${name}_${type}`);
            return;
        }

        let containerHtml = match[1]
            .replace(/w-full aspect-\d-\d/, 'w-full h-full')
            .replace(/w-full max-w-\[\d+px\] mx-auto aspect-1-1/, 'w-full h-full')
            .replace(/rounded-[a-z0-9]+/, 'rounded-none')
            .replace(/key-shadow/, '');

        const standaloneHtml = `<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="https://cdn.tailwindcss.com"></script>
<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  html, body { width: ${width}px; height: ${height}px; overflow: hidden; background: transparent; font-family: 'Plus Jakarta Sans', sans-serif; }
</style>
</head>
<body>
  <div style="width:${width}px; height:${height}px;">
    ${containerHtml}
  </div>
</body>
</html>`;

        const tempFile = path.join(outDir, `temp_set_${num}_${name}_${type}.html`);

        fs.writeFileSync(tempFile, standaloneHtml, 'utf8');

        // Use virtual-time-budget so Edge waits for Tailwind CDN & fonts to load before screenshot
        const cmd = `"${edgePath}" --headless --disable-gpu --virtual-time-budget=3000 --screenshot="${pngFile}" --window-size=${width},${height} "file://${tempFile.replace(/\\/g, '/')}"`;
        try {
            execSync(cmd, { stdio: 'ignore' });
            console.log(`✓ Generated set_${num}_${name}_${type}.png (${width}x${height})`);
        } catch (err) {
            console.error(`✗ Error generating set_${num}_${name}_${type}.png: ${err.message}`);
        }

        // Clean up temp html file
        try { fs.unlinkSync(tempFile); } catch (e) {}
    });
});

console.log('Done extracting sets 11 to 21!');
