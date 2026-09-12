Add-Type -AssemblyName System.Drawing

$resDir = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\ime\src\main\res\drawable-nodpi"
$brainDir = "C:\Users\Siku\.gemini\antigravity-ide\brain\da90d791-29e2-4098-96a9-4ebe3ea835eb"
$assetSourceDir = "C:\Users\Siku\Downloads\stitch_pizza_keyboard_ui_assets\stitch_pizza_keyboard_ui_assets"

if (-not (Test-Path $resDir)) { New-Item -ItemType Directory -Path $resDir -Force }

# 1. Copy preview image
$previewSource = Join-Path $assetSourceDir "1789054794270.png\screen.png"
if (Test-Path $previewSource) {
    Copy-Item -Path $previewSource -Destination (Join-Path $resDir "slice_pizza_keyboard_preview.png") -Force
    Copy-Item -Path $previewSource -Destination (Join-Path $brainDir "slice_pizza_keyboard_preview.png") -Force
}

# 2. Extract SVG code.html files to temporary html files and use Edge headless to render transparent PNGs for icons
$icons = @{
    "ic_saucepan" = "ic_pizza_saucepan"
    "ic_pizza_peel" = "ic_pizza_peel"
    "ic_cutter_gear" = "ic_pizza_cutter_gear"
    "ic_cheese_shaker" = "ic_pizza_cheese_shaker"
    "ic_stickers_st" = "ic_pizza_stickers_st"
    "ic_emoji_pizza" = "ic_pizza_emoji_slice"
}

$tempHtmlDir = Join-Path $env:TEMP "pizza_svg_temp"
if (-not (Test-Path $tempHtmlDir)) { New-Item -ItemType Directory -Path $tempHtmlDir -Force }

foreach ($key in $icons.Keys) {
    $srcHtml = Join-Path $assetSourceDir "$key\code.html"
    $outName = $icons[$key]
    if (Test-Path $srcHtml) {
        $svgContent = Get-Content -Raw $srcHtml
        $fullHtml = @"
<!DOCTYPE html>
<html>
<head>
<style>
  body { margin: 0; padding: 0; background: transparent; overflow: hidden; }
  svg { width: 200px; height: 200px; }
</style>
</head>
<body>
$svgContent
</body>
</html>
"@
        $htmlFile = Join-Path $tempHtmlDir "$outName.html"
        Set-Content -Path $htmlFile -Value $fullHtml -Encoding UTF8
        
        $edgePath = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
        $shotPath = Join-Path $tempHtmlDir "$outName.png"
        
        & $edgePath --headless --disable-gpu --force-device-scale-factor=1 --window-size=200,200 --screenshot=$shotPath $htmlFile | Out-Null
        
        if (Test-Path $shotPath) {
            Copy-Item -Path $shotPath -Destination (Join-Path $resDir "$outName.png") -Force
            Copy-Item -Path $shotPath -Destination (Join-Path $brainDir "$outName.png") -Force
            Write-Host "Extracted $outName.png successfully."
        }
    }
}

# Helper function to create rounded rectangle path
function New-RoundedRectanglePath ($rect, $radius) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $radius * 2
    $arc = New-Object System.Drawing.Rectangle ($rect.X, $rect.Y, $diameter, $diameter)
    
    # Top Left
    $path.AddArc($arc, 180, 90)
    # Top Right
    $arc.X = $rect.Right - $diameter
    $path.AddArc($arc, 270, 90)
    # Bottom Right
    $arc.Y = $rect.Bottom - $diameter
    $path.AddArc($arc, 0, 90)
    # Bottom Left
    $arc.X = $rect.X
    $path.AddArc($arc, 90, 90)
    $path.CloseFigure()
    return $path
}

# 3. Generate Keycaps using System.Drawing

# A. Standard Pizza Keycap (200x200)
function Generate-StandardKeycap {
    $bmp = New-Object System.Drawing.Bitmap(200, 200)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # 3D Shadow/Bevel (bottom offset)
    $shadowRect = New-Object System.Drawing.Rectangle(10, 24, 180, 166)
    $shadowPath = New-RoundedRectanglePath $shadowRect 22
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x68, 0x30, 0x0E))
    $g.FillPath($shadowBrush, $shadowPath)
    
    # Outer Crust Border
    $crustRect = New-Object System.Drawing.Rectangle(10, 10, 180, 168)
    $crustPath = New-RoundedRectanglePath $crustRect 22
    $crustBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x8E, 0x44, 0x16))
    $g.FillPath($crustBrush, $crustPath)
    
    # Inner Cheese Surface Fill
    $cheeseRect = New-Object System.Drawing.Rectangle(16, 16, 168, 156)
    $cheesePath = New-RoundedRectanglePath $cheeseRect 18
    
    $pathGrad = New-Object System.Drawing.Drawing2D.PathGradientBrush($cheesePath)
    $pathGrad.CenterColor = [System.Drawing.Color]::FromArgb(255, 0xFF, 0xF5, 0xD8)
    $pathGrad.SurroundColors = @([System.Drawing.Color]::FromArgb(255, 0xED, 0xB8, 0x68))
    $g.FillPath($pathGrad, $cheesePath)
    
    # Inner Top Highlight
    $hiRect = New-Object System.Drawing.Rectangle(20, 18, 160, 25)
    $hiBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(120, 255, 255, 255))
    $g.FillRectangle($hiBrush, $hiRect)
    
    $outPath = Join-Path $resDir "key_pizza_standard.png"
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "key_pizza_standard.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# B. Light Blue Gorgonzola Keycap (200x200)
function Generate-GorgonzolaKeycap {
    $bmp = New-Object System.Drawing.Bitmap(200, 200)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # Shadow
    $shadowRect = New-Object System.Drawing.Rectangle(10, 24, 180, 166)
    $shadowPath = New-RoundedRectanglePath $shadowRect 22
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x54, 0x6C, 0x70))
    $g.FillPath($shadowBrush, $shadowPath)
    
    # Crust Border
    $crustRect = New-Object System.Drawing.Rectangle(10, 10, 180, 168)
    $crustPath = New-RoundedRectanglePath $crustRect 22
    $crustBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x75, 0x8F, 0x93))
    $g.FillPath($crustBrush, $crustPath)
    
    # Surface
    $surfRect = New-Object System.Drawing.Rectangle(16, 16, 168, 156)
    $surfPath = New-RoundedRectanglePath $surfRect 18
    $surfGrad = New-Object System.Drawing.Drawing2D.LinearGradientBrush($surfRect, [System.Drawing.Color]::FromArgb(255, 0xDF, 0xEB, 0xEB), [System.Drawing.Color]::FromArgb(255, 0xA9, 0xC2, 0xC2), 135)
    $g.FillPath($surfGrad, $surfPath)
    
    # Gorgonzola Blue Cheese Veins
    $veinPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(160, 0x43, 0x6B, 0x78), 3)
    $g.DrawArc($veinPen, 30, 40, 80, 60, 120, 100)
    $g.DrawArc($veinPen, 100, 100, 70, 50, 200, 90)
    
    $bmp.Save((Join-Path $resDir "key_blue_gorgonzola.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "key_blue_gorgonzola.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# C. Deep Blue Enter Keycap (200x200)
function Generate-DeepBlueEnterKeycap {
    $bmp = New-Object System.Drawing.Bitmap(200, 200)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # Shadow
    $shadowRect = New-Object System.Drawing.Rectangle(10, 24, 180, 166)
    $shadowPath = New-RoundedRectanglePath $shadowRect 22
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x0E, 0x22, 0x33))
    $g.FillPath($shadowBrush, $shadowPath)
    
    # Border
    $crustRect = New-Object System.Drawing.Rectangle(10, 10, 180, 168)
    $crustPath = New-RoundedRectanglePath $crustRect 22
    $crustBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x14, 0x30, 0x48))
    $g.FillPath($crustBrush, $crustPath)
    
    # Surface
    $surfRect = New-Object System.Drawing.Rectangle(16, 16, 168, 156)
    $surfPath = New-RoundedRectanglePath $surfRect 18
    $surfGrad = New-Object System.Drawing.Drawing2D.LinearGradientBrush($surfRect, [System.Drawing.Color]::FromArgb(255, 0x43, 0x77, 0x98), [System.Drawing.Color]::FromArgb(255, 0x18, 0x3C, 0x57), 135)
    $g.FillPath($surfGrad, $surfPath)
    
    $bmp.Save((Join-Path $resDir "key_deep_blue_enter.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "key_deep_blue_enter.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# D. Ciabatta Spacebar Keycap (600x200)
function Generate-SpacebarKeycap {
    $bmp = New-Object System.Drawing.Bitmap(600, 200)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # Shadow
    $shadowRect = New-Object System.Drawing.Rectangle(10, 24, 580, 166)
    $shadowPath = New-RoundedRectanglePath $shadowRect 35
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x5E, 0x2A, 0x0B))
    $g.FillPath($shadowBrush, $shadowPath)
    
    # Crust Border
    $crustRect = New-Object System.Drawing.Rectangle(10, 10, 580, 168)
    $crustPath = New-RoundedRectanglePath $crustRect 35
    $crustBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x82, 0x3E, 0x11))
    $g.FillPath($crustBrush, $crustPath)
    
    # Surface
    $surfRect = New-Object System.Drawing.Rectangle(16, 16, 568, 156)
    $surfPath = New-RoundedRectanglePath $surfRect 30
    $surfGrad = New-Object System.Drawing.Drawing2D.LinearGradientBrush($surfRect, [System.Drawing.Color]::FromArgb(255, 0xFF, 0xF5, 0xDD), [System.Drawing.Color]::FromArgb(255, 0xBA, 0x69, 0x1A), 90)
    $g.FillPath($surfGrad, $surfPath)
    
    # Cheesy Blister spots
    $blisterBrush1 = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(140, 255, 220, 130))
    $g.FillEllipse($blisterBrush1, 80, 40, 50, 30)
    $g.FillEllipse($blisterBrush1, 460, 110, 60, 25)
    
    $bmp.Save((Join-Path $resDir "key_pizza_spacebar.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "key_pizza_spacebar.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# E. Melted Cheese Ribbon Top Toolbar Plate (1000x160)
function Generate-MeltedCheeseRibbon {
    $bmp = New-Object System.Drawing.Bitmap(1000, 160)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    $ribbonRect = New-Object System.Drawing.Rectangle(10, 10, 980, 140)
    $ribbonPath = New-RoundedRectanglePath $ribbonRect 24
    
    # Border
    $borderBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xBC, 0x6D, 0x25))
    $g.FillPath($borderBrush, $ribbonPath)
    
    # Inner Ribbon Fill
    $innerRect = New-Object System.Drawing.Rectangle(14, 14, 972, 132)
    $innerPath = New-RoundedRectanglePath $innerRect 20
    $grad = New-Object System.Drawing.Drawing2D.LinearGradientBrush($innerRect, [System.Drawing.Color]::FromArgb(255, 0xF9, 0xDD, 0xA2), [System.Drawing.Color]::FromArgb(255, 0xDE, 0x9A, 0x38), 90)
    $g.FillPath($grad, $innerPath)
    
    $bmp.Save((Join-Path $resDir "melted_cheese_ribbon.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "melted_cheese_ribbon.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

Generate-StandardKeycap
Generate-GorgonzolaKeycap
Generate-DeepBlueEnterKeycap
Generate-SpacebarKeycap
Generate-MeltedCheeseRibbon

Write-Host "All Pizza assets generated successfully!"
