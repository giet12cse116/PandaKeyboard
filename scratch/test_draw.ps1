Add-Type -AssemblyName System.Drawing

$width = 200
$height = 280
$bmp = New-Object System.Drawing.Bitmap($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

# Clear with transparent
$g.Clear([System.Drawing.Color]::Transparent)

# Draw a soft drop shadow
$shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(40, 0, 0, 0))
$shadowPath = New-Object System.Drawing.Drawing2D.GraphicsPath
$shadowPoints = @(
    (New-Object System.Drawing.PointF(20, 30)),
    (New-Object System.Drawing.PointF(180, 30)),
    (New-Object System.Drawing.PointF(180, 260)),
    (New-Object System.Drawing.PointF(20, 260))
)
$shadowPath.AddPolygon($shadowPoints)
$g.FillPath($shadowBrush, $shadowPath)

$outputPath = "$PSScriptRoot\test_out.png"
$bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()

Write-Output "Saved test image to $outputPath"
