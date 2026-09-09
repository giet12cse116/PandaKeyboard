$jsonPath = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\core-theme\src\main\assets\themes.json"
$jsonContent = Get-Content -Path $jsonPath -Raw -Encoding UTF8
$data = $jsonContent | ConvertFrom-Json

$customKeysThemes = @(
    [PSCustomObject]@{
        id = "custom_keys_starry_purple"
        name = "Starry Lavender Keys"
        isPro = $true
        keyBackgroundColor = "#40B388FF"
        keyTextColor = "#FFFFFF"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#311B92", "#512DA8", "#673AB7")
            angle = 135
        }
        keyShape = "rounded"
        accentColor = "#B388FF"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_pink_cloud"
        name = "Cute Pink Cloud Keys"
        isPro = $true
        keyBackgroundColor = "#60FFFFFF"
        keyTextColor = "#4A1B4D"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#FFE5EC", "#FFC2D1", "#FFB3C6")
            angle = 45
        }
        keyShape = "pill"
        accentColor = "#FF4081"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_winter_moon"
        name = "Crescent Moon Keys"
        isPro = $true
        keyBackgroundColor = "#451C2541"
        keyTextColor = "#FFD54F"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#0B132B", "#1C2541", "#3A506B")
            angle = 135
        }
        keyShape = "pill"
        accentColor = "#FFD54F"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_christmas_gift"
        name = "Holiday Gift Box Keys"
        isPro = $true
        keyBackgroundColor = "#5566BB6A"
        keyTextColor = "#E8F5E9"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#1B2A21", "#2D4A3E", "#388E3C")
            angle = 0
        }
        keyShape = "rounded"
        accentColor = "#66BB6A"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_butterfly_glow"
        name = "Butterfly Glow Keys"
        isPro = $true
        keyBackgroundColor = "#50FF9A9E"
        keyTextColor = "#4A148C"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#FFDEE9", "#B5FFFC", "#FFB6C1")
            angle = 45
        }
        keyShape = "pill"
        accentColor = "#FF1744"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_santa_emerald"
        name = "Emerald Santa Keys"
        isPro = $true
        keyBackgroundColor = "#60D50000"
        keyTextColor = "#FFD700"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#051710", "#0E291E", "#1B523D")
            angle = 135
        }
        keyShape = "rounded"
        accentColor = "#FF3D00"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_gingerbread"
        name = "Gingerbread House Keys"
        isPro = $true
        keyBackgroundColor = "#55FF7043"
        keyTextColor = "#FFF3E0"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#210702", "#3D1308", "#78220A")
            angle = 45
        }
        keyShape = "pill"
        accentColor = "#FF9100"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_mint_dream"
        name = "Translucent Mint Keys"
        isPro = $false
        keyBackgroundColor = "#4000E676"
        keyTextColor = "#0E291E"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#E0F2F1", "#B2DFDB", "#80CBC4")
            angle = 90
        }
        keyShape = "rounded"
        accentColor = "#1DE9B6"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_retro_3d"
        name = "Retro 3D Arcade Keys"
        isPro = $true
        keyBackgroundColor = "#CC2A2A3C"
        keyTextColor = "#00F5FF"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#05021A", "#121212", "#1E1E2E")
            angle = 45
        }
        keyShape = "square"
        accentColor = "#FF2A6D"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_cat_paw"
        name = "Golden Cat Paw Keys"
        isPro = $true
        keyBackgroundColor = "#80FFECB3"
        keyTextColor = "#3E272A"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#FFF9C4", "#FFE082", "#FFD54F")
            angle = 90
        }
        keyShape = "rounded"
        accentColor = "#FFA000"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_autumn_fox"
        name = "Autumn Fox Keys"
        isPro = $true
        keyBackgroundColor = "#50FF7043"
        keyTextColor = "#FFE0B2"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#1A0A2E", "#3D2040", "#6D2077")
            angle = 135
        }
        keyShape = "pill"
        accentColor = "#FF5722"
        category = "Custom Keys"
    },
    [PSCustomObject]@{
        id = "custom_keys_cute_bear"
        name = "Cute Bear Cream Keys"
        isPro = $false
        keyBackgroundColor = "#80D7CCC8"
        keyTextColor = "#4E342E"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#FFF8E7", "#F5E6D3", "#E6D2B8")
            angle = 0
        }
        keyShape = "pill"
        accentColor = "#8D6E63"
        category = "Custom Keys"
    }
)

# Remove any existing custom_keys_* themes to prevent duplicates
$existingThemes = @($data.themes | Where-Object { $_.category -ne "Custom Keys" })

# Combine custom keys themes first, followed by existing themes
$data.themes = @($customKeysThemes + $existingThemes)

Write-Host "Total themes now: $($data.themes.Count)"

$newJson = $data | ConvertTo-Json -Depth 100
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($jsonPath, $newJson, $utf8NoBom)
Write-Host "Successfully updated themes.json with Custom Keys category!"
