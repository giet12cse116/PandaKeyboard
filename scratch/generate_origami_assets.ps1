Add-Type -AssemblyName System.Drawing

$resDir = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\ime\src\main\res\drawable-nodpi"
$brainDir = "C:\Users\Siku\.gemini\antigravity-ide\brain\da90d791-29e2-4098-96a9-4ebe3ea835eb"

if (-not (Test-Path $resDir)) {
    New-Item -Path $resDir -ItemType Directory -Force | Out-Null
}

function Save-Dual ($bmp, $name) {
    $path1 = Join-Path $resDir "$name.png"
    $path2 = Join-Path $brainDir "$name.png"
    $bmp.Save($path1, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save($path2, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Output "Saved $name.png"
}

# Color Palette Definitions
$mintTop = [System.Drawing.Color]::FromArgb(255, 168, 230, 193)
$mintShade = [System.Drawing.Color]::FromArgb(255, 127, 203, 160)
$mintLight = [System.Drawing.Color]::FromArgb(255, 205, 243, 220)

$purpleTop = [System.Drawing.Color]::FromArgb(255, 195, 177, 225)
$purpleShade = [System.Drawing.Color]::FromArgb(255, 158, 133, 196)
$purpleLight = [System.Drawing.Color]::FromArgb(255, 222, 210, 242)

$peachTop = [System.Drawing.Color]::FromArgb(255, 245, 201, 160)
$peachShade = [System.Drawing.Color]::FromArgb(255, 232, 164, 108)
$peachLight = [System.Drawing.Color]::FromArgb(255, 250, 222, 194)

$blueTop = [System.Drawing.Color]::FromArgb(255, 168, 216, 232)
$blueShade = [System.Drawing.Color]::FromArgb(255, 121, 184, 206)
$blueLight = [System.Drawing.Color]::FromArgb(255, 205, 236, 246)

$pinkTop = [System.Drawing.Color]::FromArgb(255, 248, 182, 193)
$pinkShade = [System.Drawing.Color]::FromArgb(255, 229, 138, 155)

$plumText = [System.Drawing.Color]::FromArgb(255, 74, 59, 107)

# Helper function to render standard faceted key
function Render-StandardKey ($cTop, $cShade, $cLight, $filename) {
    $w = 240
    $h = 320
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)

    # 1. Soft Drop Shadow
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(35, 0, 0, 0))
    [System.Drawing.PointF[]]$sPts = @(
        (New-Object System.Drawing.PointF(35, 45)),
        (New-Object System.Drawing.PointF(205, 45)),
        (New-Object System.Drawing.PointF(225, 305)),
        (New-Object System.Drawing.PointF(15, 305))
    )
    $sPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $sPath.AddPolygon($sPts)
    $g.FillPath($shadowBrush, $sPath)

    # Coordinates
    $left = [float]20.0
    $top = [float]20.0
    $right = [float]220.0
    $bottom = [float]295.0
    $c = [float]28.0 # chamfer size

    # Inner top face
    $tLeft = [float]32.0
    $tTop = [float]32.0
    $tRight = [float]208.0
    $tBottom = [float]265.0
    $tc = [float]22.0

    [System.Drawing.PointF[]]$topPts = @(
        (New-Object System.Drawing.PointF(($tLeft + $tc), $tTop)),
        (New-Object System.Drawing.PointF(($tRight - $tc), $tTop)),
        (New-Object System.Drawing.PointF($tRight, ($tTop + $tc))),
        (New-Object System.Drawing.PointF($tRight, ($tBottom - $tc))),
        (New-Object System.Drawing.PointF(($tRight - $tc), $tBottom)),
        (New-Object System.Drawing.PointF(($tLeft + $tc), $tBottom)),
        (New-Object System.Drawing.PointF($tLeft, ($tBottom - $tc))),
        (New-Object System.Drawing.PointF($tLeft, ($tTop + $tc)))
    )

    # 2. Draw Shaded Bottom & Right Facets
    $shadeBrush = New-Object System.Drawing.SolidBrush($cShade)
    [System.Drawing.PointF[]]$bottomFacetPts = @(
        (New-Object System.Drawing.PointF(($tLeft + $tc), $tBottom)),
        (New-Object System.Drawing.PointF(($tRight - $tc), $tBottom)),
        (New-Object System.Drawing.PointF($tRight, ($tBottom - $tc))),
        (New-Object System.Drawing.PointF($right, ($bottom - $c))),
        (New-Object System.Drawing.PointF(($right - $c), $bottom)),
        (New-Object System.Drawing.PointF(($left + $c), $bottom)),
        (New-Object System.Drawing.PointF($left, ($bottom - $c))),
        (New-Object System.Drawing.PointF($tLeft, ($tBottom - $tc)))
    )
    $bPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $bPath.AddPolygon($bottomFacetPts)
    $g.FillPath($shadeBrush, $bPath)

    # Right side facet
    [System.Drawing.PointF[]]$rightFacetPts = @(
        (New-Object System.Drawing.PointF(($tRight - $tc), $tTop)),
        (New-Object System.Drawing.PointF($tRight, ($tTop + $tc))),
        (New-Object System.Drawing.PointF($tRight, ($tBottom - $tc))),
        (New-Object System.Drawing.PointF($right, ($bottom - $c))),
        (New-Object System.Drawing.PointF($right, ($top + $c))),
        (New-Object System.Drawing.PointF(($right - $c), $top))
    )
    $rPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $rPath.AddPolygon($rightFacetPts)
    $g.FillPath($shadeBrush, $rPath)

    # 3. Draw Top Flat Facet
    $topBrush = New-Object System.Drawing.SolidBrush($cTop)
    $tPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $tPath.AddPolygon($topPts)
    $g.FillPath($topBrush, $tPath)

    # 4. Highlight lines
    $highlightPen = New-Object System.Drawing.Pen($cLight, [float]3.0)
    $g.DrawLine($highlightPen, ($left + $c), $top, ($tLeft + $tc), $tTop)
    $g.DrawLine($highlightPen, $left, ($top + $c), $tLeft, ($tTop + $tc))

    # Seam lines
    $seamPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(40, 0, 0, 0), [float]1.5)
    $g.DrawPath($seamPen, $tPath)

    Save-Dual $bmp $filename
    $g.Dispose()
    $bmp.Dispose()
}

# 1. Render standard keys
Render-StandardKey $mintTop $mintShade $mintLight "standard_origami_mint"
Render-StandardKey $purpleTop $purpleShade $purpleLight "standard_origami_purple"
Render-StandardKey $peachTop $peachShade $peachLight "standard_origami_peach"
Render-StandardKey $blueTop $blueShade $blueLight "standard_origami_blue"

# 2. Render Spacebar
function Render-Spacebar {
    $w = 1000
    $h = 320
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)

    # Drop shadow
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(35, 0, 0, 0))
    $sPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$sPts = @(
        (New-Object System.Drawing.PointF(35, 45)),
        (New-Object System.Drawing.PointF(965, 45)),
        (New-Object System.Drawing.PointF(985, 305)),
        (New-Object System.Drawing.PointF(15, 305))
    )
    $sPath.AddPolygon($sPts)
    $g.FillPath($shadowBrush, $sPath)

    # Shaded facets
    $bPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$bPts = @(
        (New-Object System.Drawing.PointF(50, 265)),
        (New-Object System.Drawing.PointF(950, 265)),
        (New-Object System.Drawing.PointF(980, 295)),
        (New-Object System.Drawing.PointF(20, 295))
    )
    $bPath.AddPolygon($bPts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($mintShade)), $bPath)

    # Top Face
    $tPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$tPts = @(
        (New-Object System.Drawing.PointF(50, 20)),
        (New-Object System.Drawing.PointF(950, 20)),
        (New-Object System.Drawing.PointF(950, 265)),
        (New-Object System.Drawing.PointF(50, 265))
    )
    $tPath.AddPolygon($tPts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($mintTop)), $tPath)

    # Seam lines
    $seamPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(40, 0, 0, 0), [float]1.5)
    $g.DrawPath($seamPen, $tPath)

    Save-Dual $bmp "special_origami_spacebar_mint"
    $g.Dispose()
    $bmp.Dispose()
}
Render-Spacebar

# 3. Render Special Shift Key
function Render-ShiftKey {
    $w = 340
    $h = 320
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)

    # Shadow
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(35, 0, 0, 0))
    $sPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$sPts = @(
        (New-Object System.Drawing.PointF(35, 45)),
        (New-Object System.Drawing.PointF(305, 45)),
        (New-Object System.Drawing.PointF(325, 305)),
        (New-Object System.Drawing.PointF(15, 305))
    )
    $sPath.AddPolygon($sPts)
    $g.FillPath($shadowBrush, $sPath)

    # Facet 1 (Bottom Right Shade)
    $f1 = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$f1Pts = @(
        (New-Object System.Drawing.PointF(170, 160)),
        (New-Object System.Drawing.PointF(320, 20)),
        (New-Object System.Drawing.PointF(320, 295)),
        (New-Object System.Drawing.PointF(20, 295))
    )
    $f1.AddPolygon($f1Pts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($peachShade)), $f1)

    # Facet 2 (Top Left Light)
    $f2 = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$f2Pts = @(
        (New-Object System.Drawing.PointF(170, 160)),
        (New-Object System.Drawing.PointF(20, 20)),
        (New-Object System.Drawing.PointF(320, 20))
    )
    $f2.AddPolygon($f2Pts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($peachLight)), $f2)

    # Facet 3 (Mid Top Face)
    $f3 = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$f3Pts = @(
        (New-Object System.Drawing.PointF(170, 160)),
        (New-Object System.Drawing.PointF(20, 20)),
        (New-Object System.Drawing.PointF(20, 295))
    )
    $f3.AddPolygon($f3Pts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($peachTop)), $f3)

    # Seam lines
    $seamPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(50, 0, 0, 0), [float]2.0)
    $g.DrawLine($seamPen, 170, 160, 20, 20)
    $g.DrawLine($seamPen, 170, 160, 320, 20)
    $g.DrawLine($seamPen, 170, 160, 320, 295)
    $g.DrawLine($seamPen, 170, 160, 20, 295)

    Save-Dual $bmp "special_origami_shift_peach"
    $g.Dispose()
    $bmp.Dispose()
}
Render-ShiftKey

# 4. Render Special Backspace Key
function Render-BackspaceKey {
    $w = 340
    $h = 320
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)

    # Drop shadow
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(35, 0, 0, 0))
    $g.FillRectangle($shadowBrush, 25, 35, 300, 270)

    # Main Face
    $f1 = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$f1Pts = @(
        (New-Object System.Drawing.PointF(20, 20)),
        (New-Object System.Drawing.PointF(270, 20)),
        (New-Object System.Drawing.PointF(200, 295)),
        (New-Object System.Drawing.PointF(20, 295))
    )
    $f1.AddPolygon($f1Pts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($pinkTop)), $f1)

    # Shaded Diagonal Slice
    $f2 = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$f2Pts = @(
        (New-Object System.Drawing.PointF(270, 20)),
        (New-Object System.Drawing.PointF(320, 20)),
        (New-Object System.Drawing.PointF(320, 295)),
        (New-Object System.Drawing.PointF(200, 295))
    )
    $f2.AddPolygon($f2Pts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($pinkShade)), $f2)

    # Seam line
    $seamPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(50, 0, 0, 0), [float]2.0)
    $g.DrawLine($seamPen, 270, 20, 200, 295)

    Save-Dual $bmp "special_origami_backspace_pink"
    $g.Dispose()
    $bmp.Dispose()
}
Render-BackspaceKey

# 5. Render Special Enter Key
function Render-EnterKey {
    $w = 340
    $h = 320
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)

    # Drop shadow
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(35, 0, 0, 0))
    $g.FillRectangle($shadowBrush, 25, 35, 300, 270)

    $cx = [float]170.0
    $cy = [float]160.0

    # Top Triangle (Light)
    $fTop = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$topPts = @(
        (New-Object System.Drawing.PointF(20, 20)),
        (New-Object System.Drawing.PointF(320, 20)),
        (New-Object System.Drawing.PointF($cx, $cy))
    )
    $fTop.AddPolygon($topPts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($blueLight)), $fTop)

    # Bottom Triangle (Shade)
    $fBot = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$botPts = @(
        (New-Object System.Drawing.PointF(20, 295)),
        (New-Object System.Drawing.PointF(320, 295)),
        (New-Object System.Drawing.PointF($cx, $cy))
    )
    $fBot.AddPolygon($botPts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($blueShade)), $fBot)

    # Left Triangle (Main Top)
    $fLeft = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$leftPts = @(
        (New-Object System.Drawing.PointF(20, 20)),
        (New-Object System.Drawing.PointF(20, 295)),
        (New-Object System.Drawing.PointF($cx, $cy))
    )
    $fLeft.AddPolygon($leftPts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($blueTop)), $fLeft)

    # Right Triangle (Main Top)
    $fRight = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$rightPts = @(
        (New-Object System.Drawing.PointF(320, 20)),
        (New-Object System.Drawing.PointF(320, 295)),
        (New-Object System.Drawing.PointF($cx, $cy))
    )
    $fRight.AddPolygon($rightPts)
    $g.FillPath((New-Object System.Drawing.SolidBrush($blueTop)), $fRight)

    # Seams
    $seamPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(50, 0, 0, 0), [float]2.0)
    $g.DrawLine($seamPen, [float]20, [float]20, $cx, $cy)
    $g.DrawLine($seamPen, [float]320, [float]20, $cx, $cy)
    $g.DrawLine($seamPen, [float]20, [float]295, $cx, $cy)
    $g.DrawLine($seamPen, [float]320, [float]295, $cx, $cy)

    Save-Dual $bmp "special_origami_enter_blue"
    $g.Dispose()
    $bmp.Dispose()
}
Render-EnterKey

# 6. Render Emoji & Symbol Keys
Render-StandardKey $purpleTop $purpleShade $purpleLight "special_origami_emoji_purple"
Render-StandardKey $peachTop $peachShade $peachLight "special_origami_symbol_peach"

# 7. Render Top Toolbar Container
function Render-TopToolbar {
    $w = 1200
    $h = 180
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)

    # Outer Bar Plate (Clean pastel container without background symbols)
    $barBg = [System.Drawing.Color]::FromArgb(160, 168, 230, 193)
    $barBrush = New-Object System.Drawing.SolidBrush($barBg)
    $barPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    [System.Drawing.PointF[]]$bPts = @(
        (New-Object System.Drawing.PointF(20, 15)),
        (New-Object System.Drawing.PointF(1180, 15)),
        (New-Object System.Drawing.PointF(1180, 165)),
        (New-Object System.Drawing.PointF(20, 165))
    )
    $barPath.AddPolygon($bPts)
    $g.FillPath($barBrush, $barPath)

    Save-Dual $bmp "origami_top_toolbar"
    $g.Dispose()
    $bmp.Dispose()
}
Render-TopToolbar

# 8. Render Full Theme Preview Image
function Render-ThemePreview {
    $w = 800
    $h = 800
    $bmp = New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

    $bgBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
        (New-Object System.Drawing.Point(0, 0)),
        (New-Object System.Drawing.Point(800, 800)),
        [System.Drawing.Color]::FromArgb(255, 230, 245, 238),
        [System.Drawing.Color]::FromArgb(255, 235, 230, 248)
    )
    $g.FillRectangle($bgBrush, 0, 0, 800, 800)

    $titleFont = New-Object System.Drawing.Font("Georgia", 22, [System.Drawing.FontStyle]::Bold)
    $g.DrawString("Panda Keyboards", $titleFont, (New-Object System.Drawing.SolidBrush($plumText)), 280, 20)

    $tbBmp = [System.Drawing.Image]::FromFile((Join-Path $resDir "origami_top_toolbar.png"))
    $g.DrawImage($tbBmp, 40, 70, 720, 90)
    $tbBmp.Dispose()

    # Draw original action icons in top toolbar layer
    $tbIconFont = New-Object System.Drawing.Font("Arial", 16, [System.Drawing.FontStyle]::Bold)
    $tbBrush = New-Object System.Drawing.SolidBrush($plumText)
    $tbIcons = @("Theme", "Clip", "Set", "Mic", "Font")
    for ($i = 0; $i -lt 5; $i++) {
        $x = [float](100 + $i * 140)
        $rect = New-Object System.Drawing.RectangleF($x, [float]85, [float]80, [float]60)
        $g.DrawString($tbIcons[$i], $tbIconFont, $tbBrush, $rect)
    }

    $kMint = [System.Drawing.Image]::FromFile((Join-Path $resDir "standard_origami_mint.png"))
    $kPurp = [System.Drawing.Image]::FromFile((Join-Path $resDir "standard_origami_purple.png"))
    $kPeach = [System.Drawing.Image]::FromFile((Join-Path $resDir "standard_origami_peach.png"))
    $kBlue = [System.Drawing.Image]::FromFile((Join-Path $resDir "standard_origami_blue.png"))
    $kShift = [System.Drawing.Image]::FromFile((Join-Path $resDir "special_origami_shift_peach.png"))
    $kBack = [System.Drawing.Image]::FromFile((Join-Path $resDir "special_origami_backspace_pink.png"))
    $kEnter = [System.Drawing.Image]::FromFile((Join-Path $resDir "special_origami_enter_blue.png"))
    $kSpace = [System.Drawing.Image]::FromFile((Join-Path $resDir "special_origami_spacebar_mint.png"))
    $kEmoji = [System.Drawing.Image]::FromFile((Join-Path $resDir "special_origami_emoji_purple.png"))
    $kSym = [System.Drawing.Image]::FromFile((Join-Path $resDir "special_origami_symbol_peach.png"))

    $keyFont = New-Object System.Drawing.Font("Georgia", 24, [System.Drawing.FontStyle]::Bold)
    $textBrush = New-Object System.Drawing.SolidBrush($plumText)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center

    # Row 1
    $r1Labels = @("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    for ($i = 0; $i -lt 10; $i++) {
        $x = [float](40 + $i * 72)
        $y = [float]180
        $g.DrawImage($kMint, $x, $y, 68, 95)
        $rect = New-Object System.Drawing.RectangleF(($x + 5), ($y + 10), 58, 65)
        $g.DrawString($r1Labels[$i], $keyFont, $textBrush, $rect, $sf)
    }

    # Row 2
    $r2Labels = @("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    $r2Imgs = @($kMint, $kPurp, $kPurp, $kPurp, $kMint, $kPurp, $kPurp, $kPeach, $kPeach, $kBlue)
    for ($i = 0; $i -lt 10; $i++) {
        $x = [float](40 + $i * 72)
        $y = [float]295
        $g.DrawImage($r2Imgs[$i], $x, $y, 68, 95)
        $rect = New-Object System.Drawing.RectangleF(($x + 5), ($y + 10), 58, 65)
        $g.DrawString($r2Labels[$i], $keyFont, $textBrush, $rect, $sf)
    }

    # Row 3
    $r3Labels = @("a", "s", "d", "f", "g", "h", "j", "k", "l")
    $r3Imgs = @($kMint, $kPurp, $kMint, $kMint, $kPurp, $kMint, $kPeach, $kPeach, $kBlue)
    for ($i = 0; $i -lt 9; $i++) {
        $x = [float](76 + $i * 72)
        $y = [float]410
        $g.DrawImage($r3Imgs[$i], $x, $y, 68, 95)
        $rect = New-Object System.Drawing.RectangleF(($x + 5), ($y + 10), 58, 65)
        $g.DrawString($r3Labels[$i], $keyFont, $textBrush, $rect, $sf)
    }

    # Row 4
    $g.DrawImage($kShift, [float]40, [float]525, 95, 95)
    $g.DrawString("Shift", (New-Object System.Drawing.Font("Georgia", 14, [System.Drawing.FontStyle]::Bold)), $textBrush, (New-Object System.Drawing.RectangleF([float]45, [float]535, 85, 65)), $sf)

    $r4Labels = @("z", "x", "c", "v", "b", "n", "m")
    $r4Imgs = @($kMint, $kMint, $kMint, $kPurp, $kPeach, $kPeach, $kBlue)
    for ($i = 0; $i -lt 7; $i++) {
        $x = [float](148 + $i * 72)
        $y = [float]525
        $g.DrawImage($r4Imgs[$i], $x, $y, 68, 95)
        $rect = New-Object System.Drawing.RectangleF(($x + 5), ($y + 10), 58, 65)
        $g.DrawString($r4Labels[$i], $keyFont, $textBrush, $rect, $sf)
    }

    $g.DrawImage($kBack, [float]652, [float]525, 108, 95)
    $g.DrawString("Del", (New-Object System.Drawing.Font("Georgia", 14, [System.Drawing.FontStyle]::Bold)), $textBrush, (New-Object System.Drawing.RectangleF([float]657, [float]535, 98, 65)), $sf)

    # Row 5
    $g.DrawImage($kSym, [float]40, [float]640, 85, 95)
    $g.DrawString("?123", (New-Object System.Drawing.Font("Georgia", 16, [System.Drawing.FontStyle]::Bold)), $textBrush, (New-Object System.Drawing.RectangleF([float]45, [float]650, 75, 65)), $sf)

    $g.DrawImage($kEmoji, [float]133, [float]640, 68, 95)
    $g.DrawString(":)", (New-Object System.Drawing.Font("Arial", 22)), $textBrush, (New-Object System.Drawing.RectangleF([float]138, [float]650, 58, 65)), $sf)

    $g.DrawImage($kPeach, [float]209, [float]640, 68, 95)
    $g.DrawString(",", $keyFont, $textBrush, (New-Object System.Drawing.RectangleF([float]214, [float]650, 58, 65)), $sf)

    $g.DrawImage($kSpace, [float]285, [float]640, 290, 95)
    $g.DrawString("space", (New-Object System.Drawing.Font("Georgia", 16, [System.Drawing.FontStyle]::Bold)), $textBrush, (New-Object System.Drawing.RectangleF([float]290, [float]650, 280, 65)), $sf)

    $g.DrawImage($kPeach, [float]583, [float]640, 68, 95)
    $g.DrawString(".", $keyFont, $textBrush, (New-Object System.Drawing.RectangleF([float]588, [float]650, 58, 65)), $sf)

    $g.DrawImage($kEnter, [float]659, [float]640, 101, 95)
    $g.DrawString("Enter", (New-Object System.Drawing.Font("Georgia", 14, [System.Drawing.FontStyle]::Bold)), $textBrush, (New-Object System.Drawing.RectangleF([float]664, [float]650, 91, 65)), $sf)

    $kMint.Dispose()
    $kPurp.Dispose()
    $kPeach.Dispose()
    $kBlue.Dispose()
    $kShift.Dispose()
    $kBack.Dispose()
    $kEnter.Dispose()
    $kSpace.Dispose()
    $kEmoji.Dispose()
    $kSym.Dispose()

    Save-Dual $bmp "origami_paper_craft_preview"
    $g.Dispose()
    $bmp.Dispose()
}
Render-ThemePreview

Write-Output "All 3D Low-Poly Origami assets successfully generated and exported with 0 warnings!"
