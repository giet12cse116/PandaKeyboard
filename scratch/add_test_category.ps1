$jsonPath = "c:\Users\Siku\.gemini\antigravity\scratch\Panda Keyboard\core-theme\src\main\assets\themes.json"
$jsonContent = Get-Content -Path $jsonPath -Raw -Encoding UTF8
$data = $jsonContent | ConvertFrom-Json

$testThemes = @(
    [PSCustomObject]@{
        id = "test_neobrutalism"
        name = "Neo-Brutalism"
        isPro = $false
        keyBackgroundColor = "#FFE600"
        keyTextColor = "#000000"
        keyboardBackground = [PSCustomObject]@{
            type = "solid"
            color = "#FF0055"
        }
        keyShape = "square"
        accentColor = "#000000"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_glassmorphism"
        name = "Glassmorphism"
        isPro = $false
        keyBackgroundColor = "#35FFFFFF"
        keyTextColor = "#FFFFFF"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#0F172A", "#1E293B", "#38BDF8")
            angle = 135
        }
        keyShape = "rounded"
        accentColor = "#38BDF8"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_liquid_glass"
        name = "Liquid Glass"
        isPro = $false
        keyBackgroundColor = "#4000E676"
        keyTextColor = "#0E291E"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#E0F2F1", "#80CBC4", "#004D40")
            angle = 45
        }
        keyShape = "pill"
        accentColor = "#1DE9B6"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_neumorphism"
        name = "Neumorphism Soft UI"
        isPro = $false
        keyBackgroundColor = "#E0E5EC"
        keyTextColor = "#2A3B50"
        keyboardBackground = [PSCustomObject]@{
            type = "solid"
            color = "#D1D9E6"
        }
        keyShape = "rounded"
        accentColor = "#4A6CF7"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_claymorphism"
        name = "Claymorphism 3D"
        isPro = $false
        keyBackgroundColor = "#FFB6C1"
        keyTextColor = "#4A1B4D"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#FFF0F5", "#FFDEE9", "#B5FFFC")
            angle = 90
        }
        keyShape = "pill"
        accentColor = "#FF4081"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_tactile_skeuomorphism"
        name = "Tactile Neo-Skeuo"
        isPro = $false
        keyBackgroundColor = "#2A2D34"
        keyTextColor = "#E4E7EB"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#141619", "#22252A", "#3A3F47")
            angle = 0
        }
        keyShape = "rounded"
        accentColor = "#FFAB00"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_y2k_cyber_chic"
        name = "Y2K Cyber-Chic"
        isPro = $false
        keyBackgroundColor = "#50FF007F"
        keyTextColor = "#00F5FF"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#0D001A", "#33004D", "#590080")
            angle = 135
        }
        keyShape = "square"
        accentColor = "#FF00AA"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_dark_futurist"
        name = "Dark Futurist Terminal"
        isPro = $false
        keyBackgroundColor = "#18181B"
        keyTextColor = "#00FF66"
        keyboardBackground = [PSCustomObject]@{
            type = "solid"
            color = "#0A0A0A"
        }
        keyShape = "square"
        accentColor = "#00FF66"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_typographic_max"
        name = "Typographic Maximalism"
        isPro = $false
        keyBackgroundColor = "#111111"
        keyTextColor = "#FFFFFF"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#FF3300", "#111111")
            angle = 90
        }
        keyShape = "square"
        accentColor = "#FF3300"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_bento_grid"
        name = "Bento Grid Layout"
        isPro = $false
        keyBackgroundColor = "#27272A"
        keyTextColor = "#F4F4F5"
        keyboardBackground = [PSCustomObject]@{
            type = "solid"
            color = "#09090B"
        }
        keyShape = "rounded"
        accentColor = "#6366F1"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_spatial_vision_ui"
        name = "Spatial Vision UI"
        isPro = $false
        keyBackgroundColor = "#40FFFFFF"
        keyTextColor = "#F8FAFC"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#0A84FF", "#000000", "#1C1C1E")
            angle = 135
        }
        keyShape = "pill"
        accentColor = "#0A84FF"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_generative_ui"
        name = "Generative Adaptive UI"
        isPro = $false
        keyBackgroundColor = "#507C4DFF"
        keyTextColor = "#FFFFFF"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#120826", "#2A093D", "#00F5FF")
            angle = 45
        }
        keyShape = "rounded"
        accentColor = "#00F5FF"
        category = "test"
    },
    [PSCustomObject]@{
        id = "test_aurora_mesh"
        name = "Aurora Mesh Gradient"
        isPro = $false
        keyBackgroundColor = "#45FFFFFF"
        keyTextColor = "#FFFFFF"
        keyboardBackground = [PSCustomObject]@{
            type = "gradient"
            colors = @("#412080", "#7B1FA2", "#A770EF", "#FFB3C6")
            angle = 45
        }
        keyShape = "pill"
        accentColor = "#E040FB"
        category = "test"
    }
)

# Remove any existing test category themes first
$existingThemes = @($data.themes | Where-Object { $_.category -ne "test" -and $_.id -notlike "test_*" })

# Category Rank:
# 1: Abstract
# 2: HD Background
# 3: Nature
# 4: Gradient
# 5: Solid
# 6: Custom
# 7: test

$categoryRank = @{
    "Abstract"      = 1
    "HD Background" = 2
    "Nature"        = 3
    "Gradient"      = 4
    "Solid"         = 5
    "Custom"        = 6
    "test"          = 7
}

$allThemesCombined = @($existingThemes + $testThemes)
$sortedThemes = $allThemesCombined | Sort-Object @{ Expression = { if ($categoryRank.ContainsKey($_.category)) { $categoryRank[$_.category] } else { 99 } } }

$data.themes = @($sortedThemes)

Write-Host "Total themes count: $($data.themes.Count)"

$grouped = $data.themes | Group-Object category
foreach ($g in $grouped) {
    Write-Host "Category: $($g.Name) - Count: $($g.Count)"
}

$newJson = $data | ConvertTo-Json -Depth 100
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($jsonPath, $newJson, $utf8NoBom)
Write-Host "Successfully added test category themes to themes.json!"
