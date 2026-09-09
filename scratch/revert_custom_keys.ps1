$jsonPath = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\core-theme\src\main\assets\themes.json"
$jsonContent = Get-Content -Path $jsonPath -Raw -Encoding UTF8
$data = $jsonContent | ConvertFrom-Json

$data.themes = @($data.themes | Where-Object { $_.category -ne "Custom Keys" -and $_.id -notlike "custom_keys_*" })

Write-Host "Total themes after removing Custom Keys: $($data.themes.Count)"

$newJson = $data | ConvertTo-Json -Depth 100
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($jsonPath, $newJson, $utf8NoBom)
Write-Host "Successfully reverted Custom Keys from themes.json!"
