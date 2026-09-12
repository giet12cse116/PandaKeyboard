Add-Type -AssemblyName System.Drawing

$resDir = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\ime\src\main\res\drawable-nodpi"
$brainDir = "C:\Users\Siku\.gemini\antigravity-ide\brain\da90d791-29e2-4098-96a9-4ebe3ea835eb"
$assetSourceDir = "C:\Users\Siku\Downloads\stitch_steampunk_keyboard_design_system\stitch_steampunk_keyboard_design_system"

if (-not (Test-Path $resDir)) { New-Item -ItemType Directory -Path $resDir -Force }

# 1. Copy preview image
$previewSource = Join-Path $assetSourceDir "1789054833909_2.jpg.jpeg\screen.png"
if (Test-Path $previewSource) {
    Copy-Item -Path $previewSource -Destination (Join-Path $resDir "steampunk_keyboard_preview.png") -Force
    Copy-Item -Path $previewSource -Destination (Join-Path $brainDir "steampunk_keyboard_preview.png") -Force
    Write-Host "Copied preview image."
}

$tempHtmlDir = Join-Path $env:TEMP "steampunk_svg_temp"
if (-not (Test-Path $tempHtmlDir)) { New-Item -ItemType Directory -Path $tempHtmlDir -Force }

# Common SVG defs header
$defsSvg = @"
<svg xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="chassisGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#ab6246"/>
      <stop offset="50%" stop-color="#88452e"/>
      <stop offset="100%" stop-color="#592b1b"/>
    </linearGradient>
    <linearGradient id="brassGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#f5d696"/>
      <stop offset="40%" stop-color="#c9974c"/>
      <stop offset="70%" stop-color="#805721"/>
      <stop offset="100%" stop-color="#d8ab60"/>
    </linearGradient>
    <radialGradient id="nixieGlow" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#fff4cc"/>
      <stop offset="30%" stop-color="#ff9900"/>
      <stop offset="70%" stop-color="#ff4400" stop-opacity="0.4"/>
      <stop offset="100%" stop-color="#ff2200" stop-opacity="0"/>
    </radialGradient>
    <linearGradient id="enamelGrad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#ffffff"/>
      <stop offset="15%" stop-color="#f7f2e4"/>
      <stop offset="85%" stop-color="#e2d6be"/>
      <stop offset="100%" stop-color="#c2b399"/>
    </linearGradient>
    <linearGradient id="leatherGrad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#5c3826"/>
      <stop offset="50%" stop-color="#402315"/>
      <stop offset="100%" stop-color="#2a150b"/>
    </linearGradient>
    <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="4" stdDeviation="3" flood-color="#000000" flood-opacity="0.7"/>
    </filter>
    <filter id="neonGlow" x="-50%" y="-50%" width="200%" height="200%">
      <feDropShadow dx="0" dy="0" stdDeviation="4" flood-color="#ff7700" flood-opacity="0.9"/>
      <feDropShadow dx="0" dy="0" stdDeviation="1" flood-color="#ffffaa" flood-opacity="0.8"/>
    </filter>
    <g id="miniScrew">
      <circle cx="0" cy="0" r="2.5" fill="url(#brassGrad)" stroke="#3e240c" stroke-width="0.5"/>
      <line x1="-1.5" y1="-1" x2="1.5" y2="1" stroke="#361f0a" stroke-width="0.7"/>
    </g>
    <g id="brassRivet">
      <circle cx="0" cy="0" r="3" fill="url(#brassGrad)" stroke="#2b1606" stroke-width="0.6"/>
      <circle cx="-0.8" cy="-0.8" r="0.8" fill="#fff" opacity="0.6"/>
    </g>
  </defs>
</svg>
"@

# Helper to render SVG snippet to PNG
function Render-SvgToPng ($outName, $svgSnippet, $width, $height) {
    $fullHtml = @"
<!DOCTYPE html>
<html>
<head>
<style>
  body { margin: 0; padding: 0; background: transparent; overflow: hidden; }
  svg { width: ${width}px; height: ${height}px; }
</style>
</head>
<body>
<svg width="$width" height="$height" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg">
  $defsSvg
  $svgSnippet
</svg>
</body>
</html>
"@
    $htmlFile = Join-Path $tempHtmlDir "$outName.html"
    Set-Content -Path $htmlFile -Value $fullHtml -Encoding UTF8
    
    $edgePath = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    $shotPath = Join-Path $tempHtmlDir "$outName.png"
    
    & $edgePath --headless --disable-gpu --force-device-scale-factor=1 --window-size=${width},${height} --screenshot=$shotPath $htmlFile | Out-Null
    
    if (Test-Path $shotPath) {
        # Make white pixels transparent
        $bmp = New-Object System.Drawing.Bitmap($shotPath)
        $bmp.MakeTransparent([System.Drawing.Color]::White)
        for ($y = 0; $y -lt $bmp.Height; $y++) {
            for ($x = 0; $x -lt $bmp.Width; $x++) {
                $pxColor = $bmp.GetPixel($x, $y)
                if ($pxColor.R -ge 245 -and $pxColor.G -ge 245 -and $pxColor.B -ge 245) {
                    $bmp.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
                }
            }
        }
        
        $destRes = Join-Path $resDir "$outName.png"
        $destBrain = Join-Path $brainDir "$outName.png"
        $bmp.Save($destRes, [System.Drawing.Imaging.ImageFormat]::Png)
        $bmp.Save($destBrain, [System.Drawing.Imaging.ImageFormat]::Png)
        $bmp.Dispose()
        Write-Host "Generated $outName.png ($width x $height)"
    }
}

# 1. Top Meter Dials (64x64)
$dialPalette = @"
<g transform="translate(4, 4)">
  <circle cx="28" cy="28" r="26" fill="#1b120c" stroke="url(#brassGrad)" stroke-width="4"/>
  <circle cx="28" cy="28" r="21" fill="none" stroke="#684f33" stroke-width="1" stroke-dasharray="2,3"/>
  <path d="M28,14 C20.3,14 14,20.3 14,28 C14,35.7 20.3,42 28,42 C29.4,42 30.5,40.9 30.5,39.5 C30.5,38.8 30.2,38.2 29.8,37.8 C29.4,37.3 29.1,36.7 29.1,36 C29.1,34.6 30.2,33.5 31.6,33.5 L34,33.5 C38.4,33.5 42,29.9 42,25.5 C42,19.2 35.7,14 28,14 Z" fill="#b9935a"/>
  <circle cx="20" cy="23" r="1.8" fill="#2b1b10"/>
  <circle cx="26" cy="19" r="1.8" fill="#2b1b10"/>
  <circle cx="33" cy="22" r="1.8" fill="#2b1b10"/>
</g>
"@
Render-SvgToPng "ic_dial_theme" $dialPalette 64 64

$dialClip = @"
<g transform="translate(4, 4)">
  <circle cx="28" cy="28" r="26" fill="#1b120c" stroke="url(#brassGrad)" stroke-width="4"/>
  <circle cx="28" cy="28" r="21" fill="none" stroke="#684f33" stroke-width="1" stroke-dasharray="2,3"/>
  <rect x="20" y="20" width="16" height="20" rx="2" fill="none" stroke="#b9935a" stroke-width="2"/>
  <path d="M24,20 L24,18 C24,16.9 24.9,16 26,16 L30,16 C31.1,16 32,16.9 32,18 L32,20" fill="none" stroke="#b9935a" stroke-width="2"/>
  <line x1="24" y1="26" x2="32" y2="26" stroke="#b9935a" stroke-width="1.8"/>
  <line x1="24" y1="31" x2="32" y2="31" stroke="#b9935a" stroke-width="1.8"/>
</g>
"@
Render-SvgToPng "ic_dial_clipboard" $dialClip 64 64

$dialGear = @"
<g transform="translate(4, 4)">
  <circle cx="28" cy="28" r="26" fill="#1b120c" stroke="url(#brassGrad)" stroke-width="4"/>
  <circle cx="28" cy="28" r="21" fill="none" stroke="#684f33" stroke-width="1" stroke-dasharray="2,3"/>
  <circle cx="28" cy="28" r="5" fill="none" stroke="#b9935a" stroke-width="3"/>
  <path d="M28,16 L28,20 M28,36 L28,40 M16,28 L20,28 M36,28 L40,28 M19.5,19.5 L22.3,22.3 M33.7,33.7 L36.5,36.5 M19.5,36.5 L22.3,33.7 M33.7,22.3 L36.5,19.5" stroke="#b9935a" stroke-width="2.8" stroke-linecap="round"/>
</g>
"@
Render-SvgToPng "ic_dial_settings" $dialGear 64 64

$dialMic = @"
<g transform="translate(4, 4)">
  <circle cx="28" cy="28" r="26" fill="#1b120c" stroke="url(#brassGrad)" stroke-width="4"/>
  <circle cx="28" cy="28" r="21" fill="none" stroke="#684f33" stroke-width="1" stroke-dasharray="2,3"/>
  <rect x="24" y="17" width="8" height="14" rx="4" fill="#b9935a"/>
  <path d="M20,25 C20,30 23,33 28,33 C33,33 36,30 36,25" fill="none" stroke="#b9935a" stroke-width="2"/>
  <line x1="28" y1="33" x2="28" y2="39" stroke="#b9935a" stroke-width="2"/>
  <line x1="23" y1="39" x2="33" y2="39" stroke="#b9935a" stroke-width="2"/>
</g>
"@
Render-SvgToPng "ic_dial_mic" $dialMic 64 64

$dialFont = @"
<g transform="translate(4, 4)">
  <circle cx="28" cy="28" r="26" fill="#1b120c" stroke="url(#brassGrad)" stroke-width="4"/>
  <circle cx="28" cy="28" r="21" fill="none" stroke="#684f33" stroke-width="1" stroke-dasharray="2,3"/>
  <text x="28" y="35" fill="#b9935a" font-size="18" font-family="'Times New Roman', serif" font-weight="bold" text-anchor="middle">Tt</text>
</g>
"@
Render-SvgToPng "ic_dial_font" $dialFont 64 64

# 2. Nixie Vacuum Tube Base (100x200)
$nixieBase = @"
<g transform="translate(32, 10)">
  <rect x="12" y="146" width="4" height="14" fill="#654a2b"/>
  <rect x="20" y="146" width="4" height="14" fill="#654a2b"/>
  <path d="M4,136 L32,136 L30,150 L6,150 Z" fill="url(#brassGrad)" stroke="#38200c" stroke-width="1"/>
  <use href="#miniScrew" x="8" y="143"/>
  <use href="#miniScrew" x="28" y="143"/>
  <rect x="4" y="8" width="28" height="128" rx="14" fill="#24140b" stroke="#ffffff" stroke-opacity="0.35" stroke-width="1.5"/>
  <rect x="8" y="14" width="20" height="116" fill="none" stroke="#523925" stroke-width="0.8" stroke-dasharray="2,2"/>
  <path d="M7,20 Q7,12 16,10" stroke="#ffffff" stroke-width="1.8" stroke-linecap="round" fill="none" opacity="0.6"/>
</g>
"@
Render-SvgToPng "base_nixie_tube" $nixieBase 100 200

# 3. Cream Enamel Keycap (160x200)
$enamelKey = @"
<g transform="translate(10, 10)">
  <rect x="0" y="0" width="140" height="180" rx="22" fill="#382618" stroke="#1d1109" stroke-width="3"/>
  <rect x="10" y="10" width="120" height="160" rx="16" fill="url(#enamelGrad)"/>
  <use href="#miniScrew" x="20" y="20"/>
  <use href="#miniScrew" x="120" y="20"/>
  <use href="#miniScrew" x="20" y="160"/>
  <use href="#miniScrew" x="120" y="160"/>
</g>
"@
Render-SvgToPng "key_base_enamel" $enamelKey 160 200

# 4. Stitched Leather Square Keycap (160x160)
$leatherSquare = @"
<g transform="translate(10, 10)">
  <rect x="0" y="0" width="140" height="140" rx="20" fill="url(#leatherGrad)" stroke="#1a0d07" stroke-width="3"/>
  <rect x="10" y="10" width="120" height="120" rx="14" fill="none" stroke="#865c3b" stroke-width="2" stroke-dasharray="6,4"/>
  <use href="#brassRivet" x="18" y="18"/>
  <use href="#brassRivet" x="122" y="18"/>
  <use href="#brassRivet" x="18" y="122"/>
  <use href="#brassRivet" x="122" y="122"/>
</g>
"@
Render-SvgToPng "key_base_leather_square" $leatherSquare 160 160

# 5. Stitched Leather Wide Keycap (240x160)
$leatherWide = @"
<g transform="translate(10, 10)">
  <rect x="0" y="0" width="220" height="140" rx="20" fill="url(#leatherGrad)" stroke="#1a0d07" stroke-width="3"/>
  <rect x="10" y="10" width="200" height="120" rx="14" fill="none" stroke="#865c3b" stroke-width="2" stroke-dasharray="6,4"/>
  <use href="#brassRivet" x="18" y="18"/>
  <use href="#brassRivet" x="202" y="18"/>
  <use href="#brassRivet" x="18" y="122"/>
  <use href="#brassRivet" x="202" y="122"/>
</g>
"@
Render-SvgToPng "key_base_leather_wide" $leatherWide 240 160

# 6. Nixie Spacebar Capsule (600x160)
$spacebarNixie = @"
<g transform="translate(10, 10)">
  <rect x="0" y="10" width="580" height="120" rx="30" fill="#1b100a" stroke="#4a2e1d" stroke-width="3"/>
  <path d="M30,20 L70,20 L70,120 L30,120 Q10,70 30,20 Z" fill="url(#brassGrad)" stroke="#38210e" stroke-width="2"/>
  <use href="#miniScrew" x="45" y="40"/>
  <use href="#miniScrew" x="45" y="100"/>
  <path d="M550,20 L510,20 L510,120 L550,120 Q570,70 550,20 Z" fill="url(#brassGrad)" stroke="#38210e" stroke-width="2"/>
  <use href="#miniScrew" x="535" y="40"/>
  <use href="#miniScrew" x="535" y="100"/>
  <rect x="70" y="24" width="440" height="92" rx="20" fill="#24140b" stroke="#ffffff44" stroke-width="2"/>
  <rect x="80" y="34" width="420" height="72" rx="12" fill="url(#nixieGlow)" opacity="0.6"/>
  <path d="M 90,70 Q 115,35 140,70 T 190,70 T 240,70 T 290,70 T 340,70 T 390,70 T 440,70 T 490,70" fill="none" stroke="#fff1cc" stroke-width="6" filter="url(#neonGlow)"/>
</g>
"@
Render-SvgToPng "spacebar_nixie_tube" $spacebarNixie 600 160

# 7. Clockwork Gears Underlay Tile (400x200)
$clockworkGears = @"
<g transform="translate(10, 10)">
  <rect x="0" y="0" width="380" height="180" rx="16" fill="#251811" stroke="#483222" stroke-width="3"/>
  <circle cx="90" cy="90" r="70" fill="none" stroke="#5d432b" stroke-width="14" stroke-dasharray="16,10"/>
  <circle cx="90" cy="90" r="44" fill="#301f14" stroke="#775536" stroke-width="5"/>
  <circle cx="90" cy="90" r="14" fill="#180e08"/>
  <circle cx="230" cy="90" r="85" fill="none" stroke="#7e5c3c" stroke-width="18" stroke-dasharray="18,12"/>
  <circle cx="230" cy="90" r="54" fill="#3b271a" stroke="#8d6844" stroke-width="6"/>
  <circle cx="230" cy="90" r="18" fill="#180e08"/>
  <circle cx="340" cy="90" r="60" fill="none" stroke="#5d432b" stroke-width="14" stroke-dasharray="14,10"/>
  <circle cx="340" cy="90" r="34" fill="#301f14"/>
</g>
"@
Render-SvgToPng "bg_clockwork_gears" $clockworkGears 400 200

# 8. Copper Header Plate (600x120)
$steampunkHeader = @"
<g transform="translate(10, 10)">
  <rect x="0" y="0" width="580" height="100" rx="20" fill="url(#chassisGrad)" stroke="#34180d" stroke-width="4"/>
  <use href="#brassRivet" x="20" y="20"/>
  <use href="#brassRivet" x="560" y="20"/>
  <use href="#brassRivet" x="20" y="80"/>
  <use href="#brassRivet" x="560" y="80"/>
  <text x="290" y="62" fill="#201008" font-family="'Times New Roman', serif" font-size="38" font-weight="bold" letter-spacing="3" text-anchor="middle">Panda Keyboards</text>
</g>
"@
Render-SvgToPng "steampunk_chassis_header" $steampunkHeader 600 120

Write-Host "All Steampunk assets generated successfully!"
