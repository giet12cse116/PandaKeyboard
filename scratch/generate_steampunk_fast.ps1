Add-Type -AssemblyName System.Drawing

$resDir = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\ime\src\main\res\drawable-nodpi"
$brainDir = "C:\Users\Siku\.gemini\antigravity-ide\brain\da90d791-29e2-4098-96a9-4ebe3ea835eb"
$assetSourceDir = "C:\Users\Siku\Downloads\stitch_steampunk_keyboard_design_system\stitch_steampunk_keyboard_design_system"

if (-not (Test-Path $resDir)) { New-Item -ItemType Directory -Path $resDir -Force }

# Copy preview image
$previewSource = Join-Path $assetSourceDir "1789054833909_2.jpg.jpeg\screen.png"
if (Test-Path $previewSource) {
    Copy-Item -Path $previewSource -Destination (Join-Path $resDir "steampunk_keyboard_preview.png") -Force
    Copy-Item -Path $previewSource -Destination (Join-Path $brainDir "steampunk_keyboard_preview.png") -Force
}

function New-RoundedRectanglePath ($rect, $radius) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $radius * 2
    $arc = New-Object System.Drawing.Rectangle ($rect.X, $rect.Y, $diameter, $diameter)
    $path.AddArc($arc, 180, 90)
    $arc.X = $rect.Right - $diameter
    $path.AddArc($arc, 270, 90)
    $arc.Y = $rect.Bottom - $diameter
    $path.AddArc($arc, 0, 90)
    $arc.X = $rect.X
    $path.AddArc($arc, 90, 90)
    $path.CloseFigure()
    return $path
}

# 1. Enamel Keycap (200x240)
function Generate-EnamelKeycap {
    $bmp = New-Object System.Drawing.Bitmap(200, 240)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # Shadow & Bronze Bezel
    $bezelRect = New-Object System.Drawing.Rectangle(10, 10, 180, 220)
    $bezelPath = New-RoundedRectanglePath $bezelRect 24
    $bezelBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x38, 0x26, 0x18))
    $g.FillPath($bezelBrush, $bezelPath)
    
    # Enamel Face
    $faceRect = New-Object System.Drawing.Rectangle(20, 20, 160, 200)
    $facePath = New-RoundedRectanglePath $faceRect 18
    $faceGrad = New-Object System.Drawing.Drawing2D.LinearGradientBrush($faceRect, [System.Drawing.Color]::FromArgb(255, 0xF5, 0xEE, 0xDC), [System.Drawing.Color]::FromArgb(255, 0xC2, 0xB3, 0x99), 90)
    $g.FillPath($faceGrad, $facePath)
    
    # Corner Screws
    $screwBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xC9, 0x97, 0x4C))
    $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 0x3E, 0x24, 0x0C), 2)
    foreach ($pt in @(@(35, 35), @(165, 35), @(35, 205), @(165, 205))) {
        $g.FillEllipse($screwBrush, $pt[0]-8, $pt[1]-8, 16, 16)
        $g.DrawEllipse($pen, $pt[0]-8, $pt[1]-8, 16, 16)
        $g.DrawLine($pen, $pt[0]-5, $pt[1]-3, $pt[0]+5, $pt[1]+3)
    }
    
    $bmp.Save((Join-Path $resDir "key_base_enamel.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "key_base_enamel.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# 2. Leather Square Keycap (200x200)
function Generate-LeatherSquare {
    $bmp = New-Object System.Drawing.Bitmap(200, 200)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    $rect = New-Object System.Drawing.Rectangle(10, 10, 180, 180)
    $path = New-RoundedRectanglePath $rect 24
    $grad = New-Object System.Drawing.Drawing2D.LinearGradientBrush($rect, [System.Drawing.Color]::FromArgb(255, 0x5C, 0x38, 0x26), [System.Drawing.Color]::FromArgb(255, 0x2A, 0x15, 0x0B), 90)
    $g.FillPath($grad, $path)
    
    # Perimeter Stitching
    $stitchPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 0x86, 0x5C, 0x3B), 3)
    $stitchPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
    $stitchRect = New-Object System.Drawing.Rectangle(24, 24, 152, 152)
    $stitchPath = New-RoundedRectanglePath $stitchRect 16
    $g.DrawPath($stitchPen, $stitchPath)
    
    # Corner Dome Rivets
    $rivetBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xC9, 0x97, 0x4C))
    foreach ($pt in @(@(32, 32), @(168, 32), @(32, 168), @(168, 168))) {
        $g.FillEllipse($rivetBrush, $pt[0]-7, $pt[1]-7, 14, 14)
    }
    
    $bmp.Save((Join-Path $resDir "key_base_leather_square.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "key_base_leather_square.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# 3. Leather Wide Keycap (300x200)
function Generate-LeatherWide {
    $bmp = New-Object System.Drawing.Bitmap(300, 200)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    $rect = New-Object System.Drawing.Rectangle(10, 10, 280, 180)
    $path = New-RoundedRectanglePath $rect 24
    $grad = New-Object System.Drawing.Drawing2D.LinearGradientBrush($rect, [System.Drawing.Color]::FromArgb(255, 0x5C, 0x38, 0x26), [System.Drawing.Color]::FromArgb(255, 0x2A, 0x15, 0x0B), 90)
    $g.FillPath($grad, $path)
    
    $stitchPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 0x86, 0x5C, 0x3B), 3)
    $stitchPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
    $stitchRect = New-Object System.Drawing.Rectangle(24, 24, 252, 152)
    $stitchPath = New-RoundedRectanglePath $stitchRect 16
    $g.DrawPath($stitchPen, $stitchPath)
    
    $rivetBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xC9, 0x97, 0x4C))
    foreach ($pt in @(@(32, 32), @(268, 32), @(32, 168), @(268, 168))) {
        $g.FillEllipse($rivetBrush, $pt[0]-7, $pt[1]-7, 14, 14)
    }
    
    $bmp.Save((Join-Path $resDir "key_base_leather_wide.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "key_base_leather_wide.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# 4. Nixie Vacuum Tube Base (120x240)
function Generate-NixieTubeBase {
    $bmp = New-Object System.Drawing.Bitmap(120, 240)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # Base Socket
    $baseBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xC9, 0x97, 0x4C))
    $g.FillRectangle($baseBrush, 20, 180, 80, 30)
    
    # Clear Glass Body
    $glassRect = New-Object System.Drawing.Rectangle(20, 10, 80, 175)
    $glassPath = New-RoundedRectanglePath $glassRect 36
    $glassBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(230, 0x24, 0x14, 0x0B))
    $g.FillPath($glassBrush, $glassPath)
    $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(100, 255, 255, 255), 2)
    $g.DrawPath($pen, $glassPath)
    
    $bmp.Save((Join-Path $resDir "base_nixie_tube.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "base_nixie_tube.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# 5. Nixie Spacebar (600x180)
function Generate-SpacebarNixie {
    $bmp = New-Object System.Drawing.Bitmap(600, 180)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # Bronze Container
    $boxRect = New-Object System.Drawing.Rectangle(10, 10, 580, 160)
    $boxPath = New-RoundedRectanglePath $boxRect 30
    $boxBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x1B, 0x10, 0x0A))
    $g.FillPath($boxBrush, $boxPath)
    
    # Endcaps
    $capBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xC9, 0x97, 0x4C))
    $g.FillRectangle($capBrush, 20, 20, 50, 140)
    $g.FillRectangle($capBrush, 530, 20, 50, 140)
    
    # Glass Chamber
    $chamberRect = New-Object System.Drawing.Rectangle(70, 25, 460, 130)
    $chamberPath = New-RoundedRectanglePath $chamberRect 20
    $chamberBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(240, 0x24, 0x14, 0x0B))
    $g.FillPath($chamberBrush, $chamberPath)
    
    # Glowing Sine Filament
    $glowPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 0xFF, 0x93, 0x26), 6)
    $pts = @()
    for ($x = 90; $x -le 510; $x += 5) {
        $y = 90 + [Math]::Sin(($x - 90) * 0.08) * 30
        $pts += New-Object System.Drawing.PointF($x, $y)
    }
    $g.DrawCurve($glowPen, $pts)
    
    $bmp.Save((Join-Path $resDir "spacebar_nixie_tube.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "spacebar_nixie_tube.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# 6. Clockwork Gears Underlay (400x200)
function Generate-ClockworkGears {
    $bmp = New-Object System.Drawing.Bitmap(400, 200)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::FromArgb(255, 0x25, 0x18, 0x11))
    
    $gearPen1 = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 0x5D, 0x43, 0x2B), 14)
    $gearPen1.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
    $g.DrawEllipse($gearPen1, 20, 20, 140, 140)
    
    $gearPen2 = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 0x7E, 0x5C, 0x3C), 18)
    $gearPen2.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
    $g.DrawEllipse($gearPen2, 145, 5, 170, 170)
    
    $bmp.Save((Join-Path $resDir "bg_clockwork_gears.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "bg_clockwork_gears.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

# 7. Meter Dials (Theme, Clipboard, Settings, Mic, Font) (100x100)
function Generate-MeterDial ($name, $symbolText) {
    $bmp = New-Object System.Drawing.Bitmap(100, 100)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)
    
    # Outer Brass Bezel Ring
    $bezelBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xC9, 0x97, 0x4C))
    $g.FillEllipse($bezelBrush, 5, 5, 90, 90)
    
    # Inner Dark Gauge Face
    $faceBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0x1B, 0x12, 0x0C))
    $g.FillEllipse($faceBrush, 12, 12, 76, 76)
    
    # Radial Tick Ring
    $tickPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 0x68, 0x4F, 0x33), 2)
    $tickPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dot
    $g.DrawEllipse($tickPen, 18, 18, 64, 64)
    
    # Centered Brass Icon Symbol
    $textBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 0xB9, 0x93, 0x5A))
    $font = New-Object System.Drawing.Font("Times New Roman", 20, [System.Drawing.FontStyle]::Bold)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($symbolText, $font, $textBrush, (New-Object System.Drawing.RectangleF(0, 0, 100, 100)), $sf)
    
    $bmp.Save((Join-Path $resDir "$name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $brainDir "$name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

Generate-EnamelKeycap
Generate-LeatherSquare
Generate-LeatherWide
Generate-NixieTubeBase
Generate-SpacebarNixie
Generate-ClockworkGears
Generate-MeterDial "ic_dial_theme" "P"
Generate-MeterDial "ic_dial_clipboard" "C"
Generate-MeterDial "ic_dial_settings" "S"
Generate-MeterDial "ic_dial_mic" "M"
Generate-MeterDial "ic_dial_font" "Tt"

Write-Host "Done"
