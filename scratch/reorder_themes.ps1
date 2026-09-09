$jsonPath = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\core-theme\src\main\assets\themes.json"
$jsonContent = Get-Content -Path $jsonPath -Raw -Encoding UTF8
$data = $jsonContent | ConvertFrom-Json

# 1. Update "nature" category to "Nature"
foreach ($theme in $data.themes) {
    if ($theme.category -eq "nature") {
        $theme.category = "Nature"
    }
}

# 2. Define category order ranking:
# 1: Abstract
# 2: HD Background
# 3: Nature
# 4: Gradient
# 5: Solid
# 6: Custom

$categoryRank = @{
    "Abstract"      = 1
    "HD Background" = 2
    "Nature"        = 3
    "Gradient"      = 4
    "Solid"         = 5
    "Custom"        = 6
}

# Print current counts by category
$grouped = $data.themes | Group-Object category
foreach ($g in $grouped) {
    Write-Host "Category: $($g.Name) - Count: $($g.Count)"
}

# Sort themes by category rank first, preserving relative original order within each category
$sortedThemes = $data.themes | Sort-Object @{ Expression = { if ($categoryRank.ContainsKey($_.category)) { $categoryRank[$_.category] } else { 99 } } }

$data.themes = @($sortedThemes)

Write-Host "--- After Sorting ---"
$groupedAfter = $data.themes | Group-Object category
foreach ($g in $groupedAfter) {
    Write-Host "Category: $($g.Name) - Count: $($g.Count)"
}

$newJson = $data | ConvertTo-Json -Depth 100
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($jsonPath, $newJson, $utf8NoBom)
Write-Host "Successfully updated themes.json!"
