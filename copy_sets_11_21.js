const fs = require('fs');
const path = require('path');

const srcDir = 'C:\\Users\\Siku\\.gemini\\antigravity\\scratch\\Panda Keyboard\\extracted_icons\\New folder';
const destDir = 'C:\\Users\\Siku\\.gemini\\antigravity\\scratch\\Panda Keyboard\\ime\\src\\main\\res\\drawable';

const mappings = [
    { srcName: 'panda', destName: 'panda' },
    { srcName: 'pig', destName: 'pig' },
    { srcName: 'flower', destName: 'flower' },
    { srcName: 'fire', destName: 'fire' },
    { srcName: 'earth', destName: 'earth' },
    { srcName: 'butterfly', destName: 'butterfly' },
    { srcName: 'football', destName: 'football' },
    { srcName: 'basketball', destName: 'basketball' },
    { srcName: 'gift', destName: 'gift' },
    { srcName: 'pad', destName: 'pad' },
    { srcName: 'pizzap', destName: 'pizzap' }
];

mappings.forEach(({ srcName, destName }) => {
    // 7-1 -> space
    const srcSpace = fs.readdirSync(srcDir).find(f => f.includes(`_${srcName}_7-1_space`));
    if (srcSpace) {
        const destSpace = `theme_${destName}_space.png`;
        fs.copyFileSync(path.join(srcDir, srcSpace), path.join(destDir, destSpace));
        console.log(`Copied ${srcSpace} -> ${destSpace}`);
    } else {
        console.error(`Missing space asset for ${srcName}`);
    }

    // 2-1 -> special
    const srcSpecial = fs.readdirSync(srcDir).find(f => f.includes(`_${srcName}_2-1_special`));
    if (srcSpecial) {
        const destSpecial = `theme_${destName}_special.png`;
        fs.copyFileSync(path.join(srcDir, srcSpecial), path.join(destDir, destSpecial));
        console.log(`Copied ${srcSpecial} -> ${destSpecial}`);
    } else {
        console.error(`Missing special asset for ${srcName}`);
    }

    // 1-1 -> emoji
    const srcEmoji = fs.readdirSync(srcDir).find(f => f.includes(`_${srcName}_1-1_mascot`));
    if (srcEmoji) {
        const destEmoji = `theme_${destName}_emoji.png`;
        fs.copyFileSync(path.join(srcDir, srcEmoji), path.join(destDir, destEmoji));
        console.log(`Copied ${srcEmoji} -> ${destEmoji}`);
    } else {
        console.error(`Missing emoji asset for ${srcName}`);
    }
});

console.log('Finished copying all 33 theme assets!');
