$classicSource = "C:\Users\Siku\Desktop\New folder\Classic Key background"
$popularSource = "C:\Users\Siku\Desktop\New folder\Popular Key background"
$destDir = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\app\src\main\res\drawable"

if (!(Test-Path $destDir)) {
    New-Item -ItemType Directory -Force -Path $destDir
}

Get-ChildItem -Path $classicSource -File | ForEach-Object {
    $cleanName = "ic_classic_" + ($_.BaseName -replace '[^a-zA-Z0-9_]', '_').ToLower() + $_.Extension.ToLower()
    Copy-Item -Path $_.FullName -Destination (Join-Path $destDir $cleanName) -Force
    Write-Host "Copied Classic: $cleanName"
}

Get-ChildItem -Path $popularSource -File | ForEach-Object {
    $cleanName = "ic_popular_" + ($_.BaseName -replace '[^a-zA-Z0-9_]', '_').ToLower() + $_.Extension.ToLower()
    Copy-Item -Path $_.FullName -Destination (Join-Path $destDir $cleanName) -Force
    Write-Host "Copied Popular: $cleanName"
}
