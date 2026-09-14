const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const htmlPath = 'C:\\Users\\Siku\\Downloads\\stitch_android_keyboard_theme_assets\\code.html';
const outDir = 'C:\\Users\\Siku\\.gemini\\antigravity\\scratch\\Panda Keyboard\\extracted_icons';
const edgePath = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';

if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
}

const htmlContent = fs.readFileSync(htmlPath, 'utf8');
const sections = htmlContent.split(/<!-- SET \d+:/i);

const themeNames = [
    'sun', 'sunflower', 'burger', 'pizza', 'cookie',
    'donut', 'cheesecake', 'mango', 'heart', 'star'
];

themeNames.forEach((name, idx) => {
    const setNumber = idx + 1;
    const sectionStr = sections[setNumber];
    if (!sectionStr) return;

    const match71 = sectionStr.match(/<!-- 7:1 Search -->[\s\S]*?(<div class="[^"]*aspect-7-1[\s\S]*?<\/div>)\s*<\/div>/);
    const match21 = sectionStr.match(/<!-- 2:1 Special Key -->[\s\S]*?(<div class="[^"]*aspect-2-1[\s\S]*?<\/div>)\s*<\/div>/);
    const match11 = sectionStr.match(/<!-- 1:1 Emoji Key -->[\s\S]*?(<div class="[^"]*aspect-1-1[\s\S]*?<\/div>)\s*<\/div>/);

    const assetItems = [
        { type: '7-1_space', match: match71, width: 700, height: 100 },
        { type: '2-1_special', match: match21, width: 400, height: 200 },
        { type: '1-1_mascot', match: match11, width: 300, height: 300 }
    ];

    assetItems.forEach(({ type, match, width, height }) => {
        if (!match || !match[1]) return;

        let containerHtml = match[1]
            .replace(/w-full aspect-\d-\d/, 'w-full h-full')
            .replace(/w-20 md:w-24 aspect-1-1/, 'w-full h-full')
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

        const tempFile = path.join(outDir, `temp_set_${setNumber}_${name}_${type}.html`);
        const pngFile = path.join(outDir, `set_${setNumber}_${name}_${type}.png`);

        fs.writeFileSync(tempFile, standaloneHtml, 'utf8');

        // Use virtual-time-budget so Edge waits for Tailwind CDN & fonts to load before screenshot
        const cmd = `"${edgePath}" --headless --disable-gpu --virtual-time-budget=3000 --screenshot="${pngFile}" --window-size=${width},${height} "file://${tempFile.replace(/\\/g, '/')}"`;
        try {
            execSync(cmd, { stdio: 'ignore' });
            console.log(`✓ Generated set_${setNumber}_${name}_${type}.png (${width}x${height})`);
        } catch (err) {
            console.error(`✗ Error: ${err.message}`);
        }

        // Clean up temp html file
        try { fs.unlinkSync(tempFile); } catch (e) {}
    });
});

console.log('Done extracting all theme icons!');
