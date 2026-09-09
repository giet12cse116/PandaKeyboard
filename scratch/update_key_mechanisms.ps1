$jsonPath = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\core-theme\src\main\assets\themes.json"
$jsonContent = Get-Content -Path $jsonPath -Raw -Encoding UTF8
$data = $jsonContent | ConvertFrom-Json

$mechanismMap = @{
    "test_neobrutalism" = @{
        keyBorderColor = "#000000"
        keyBorderWidthDp = 2.5
        keyShadowColor = "#000000"
        keyShadowOffsetDp = 3.0
    }
    "test_glassmorphism" = @{
        keyBorderColor = "#60FFFFFF"
        keyBorderWidthDp = 1.2
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_liquid_glass" = @{
        keyBorderColor = "#801DE9B6"
        keyBorderWidthDp = 1.5
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_neumorphism" = @{
        keyBorderColor = "#FFFFFF"
        keyBorderWidthDp = 1.0
        keyShadowColor = "#A3B1C6"
        keyShadowOffsetDp = 2.0
    }
    "test_claymorphism" = @{
        keyBorderColor = "#FFFFFF"
        keyBorderWidthDp = 1.5
        keyShadowColor = "#E6899A"
        keyShadowOffsetDp = 2.5
    }
    "test_tactile_skeuomorphism" = @{
        keyBorderColor = "#4E5460"
        keyBorderWidthDp = 1.5
        keyShadowColor = "#000000"
        keyShadowOffsetDp = 2.0
    }
    "test_y2k_cyber_chic" = @{
        keyBorderColor = "#00F5FF"
        keyBorderWidthDp = 1.5
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_dark_futurist" = @{
        keyBorderColor = "#00FF66"
        keyBorderWidthDp = 1.2
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_typographic_max" = @{
        keyBorderColor = "#FF3300"
        keyBorderWidthDp = 1.5
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_bento_grid" = @{
        keyBorderColor = "#3F3F46"
        keyBorderWidthDp = 1.2
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_spatial_vision_ui" = @{
        keyBorderColor = "#600A84FF"
        keyBorderWidthDp = 1.2
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_generative_ui" = @{
        keyBorderColor = "#8000F5FF"
        keyBorderWidthDp = 1.2
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
    "test_aurora_mesh" = @{
        keyBorderColor = "#80FFFFFF"
        keyBorderWidthDp = 1.2
        keyShadowColor = $null
        keyShadowOffsetDp = 0.0
    }
}

foreach ($theme in $data.themes) {
    if ($mechanismMap.ContainsKey($theme.id)) {
        $props = $mechanismMap[$theme.id]
        $theme | Add-Member -NotePropertyName "keyBorderColor" -NotePropertyValue $props.keyBorderColor -Force
        $theme | Add-Member -NotePropertyName "keyBorderWidthDp" -NotePropertyValue $props.keyBorderWidthDp -Force
        $theme | Add-Member -NotePropertyName "keyShadowColor" -NotePropertyValue $props.keyShadowColor -Force
        $theme | Add-Member -NotePropertyName "keyShadowOffsetDp" -NotePropertyValue $props.keyShadowOffsetDp -Force
    }
}

$newJson = $data | ConvertTo-Json -Depth 100
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($jsonPath, $newJson, $utf8NoBom)
Write-Host "Successfully updated themes.json with exact key mechanisms!"
