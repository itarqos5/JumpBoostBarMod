$ErrorActionPreference = "Stop"

function Write-Step($Text) {
    Write-Host ""
    Write-Host "> $Text" -ForegroundColor Green
}

function Show-ProgressBar($Label) {
    for ($i = 0; $i -le 100; $i += 4) {
        $filled = [int]($i / 4)
        $empty = 25 - $filled
        $bar = ("#" * $filled) + ("." * $empty)
        Write-Host -NoNewline "`r$Label [$bar] $i%"
        Start-Sleep -Milliseconds 25
    }
    Write-Host ""
}

function Read-Choice($Prompt, $Allowed, $Default) {
    while ($true) {
        $value = Read-Host "$Prompt [$Default]"
        if ([string]::IsNullOrWhiteSpace($value)) {
            return $Default
        }
        $value = $value.Trim().ToLowerInvariant()
        if ($Allowed -contains $value) {
            return $value
        }
        Write-Host "Choose one of: $($Allowed -join ', ')" -ForegroundColor Yellow
    }
}

function Read-GradleProperty($Name, $Default) {
    $line = Get-Content "gradle.properties" | Where-Object { $_ -match "^$Name=" } | Select-Object -First 1
    if ($line) {
        return ($line -split "=", 2)[1].Trim()
    }
    return $Default
}

function Find-ModrinthFile($Project, $Loader, $GameVersion) {
    $encodedProject = [uri]::EscapeDataString($Project)
    # Build a proper facets JSON and URL-encode it once. Avoid double-encoding individual elements.
    $facetsJson = '["project_type:mod","client_side:required","categories:' + $Loader + '","versions:' + $GameVersion + '"]'
    $encodedFacets = [uri]::EscapeDataString($facetsJson)
    $url = "https://api.modrinth.com/v2/search?limit=5&facets=$encodedFacets&query=$encodedProject"

    try {
        $searchResult = Invoke-RestMethod -Headers @{ "User-Agent" = "JumpBoostBar/dev-runner" } -Uri $url -ErrorAction Stop
        if ($searchResult.total_hits -eq 0) {
            Write-Host "No project found for '$Project' on Modrinth." -ForegroundColor Yellow
            return $null
        }
        $projectSlug = $searchResult.hits[0].slug
    } catch {
        Write-Host "Error searching for project '$Project' on Modrinth: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }

    $encodedLoader = [uri]::EscapeDataString($Loader)
    $encodedGameVersion = [uri]::EscapeDataString($GameVersion)
    $versionsUrl = "https://api.modrinth.com/v2/project/$projectSlug/version?loaders=$encodedLoader&game_versions=$encodedGameVersion"
    try {
        $versions = Invoke-RestMethod -Headers @{ "User-Agent" = "JumpBoostBar/dev-runner" } -Uri $versionsUrl -ErrorAction Stop
    } catch {
        Write-Host "Could not fetch versions for '$projectSlug': $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }

    $sortedVersions = $versions | Sort-Object -Property date_published -Descending
    $release = $sortedVersions | Where-Object { $_.version_type -eq 'release' } | Select-Object -First 1
    $bestMatch = if ($release) { $release } else { $sortedVersions | Select-Object -First 1 }

    if (-not $bestMatch) {
        return $null
    }

    $primaryFile = $bestMatch.files | Where-Object { $_.primary } | Select-Object -First 1
    return if ($primaryFile) { $primaryFile } else { $bestMatch.files | Select-Object -First 1 }
}

function Resolve-ModrinthGameVersion($GameVersion) {
    try {
        $versions = Invoke-RestMethod -Headers @{ "User-Agent" = "JumpBoostBar/dev-runner" } -Uri "https://api.modrinth.com/v2/tag/game_version" -ErrorAction Stop
    } catch {
        Write-Host "Could not fetch Modrinth game version tags; using '$GameVersion'." -ForegroundColor Yellow
        return $GameVersion
    }

    $available = @()
    foreach ($entry in $versions) {
        if ($entry -is [string]) {
            $available += $entry
        } elseif ($entry.PSObject.Properties.Name -contains "version") {
            $available += [string]$entry.version
        }
    }

    if ($available -contains $GameVersion) {
        return $GameVersion
    }

    if ($GameVersion -match '^(\d+\.\d+)\.(\d)(\d+)$') {
        $collapsedPatch = "$($Matches[1]).$($Matches[2])"
        if ($available -contains $collapsedPatch) {
            Write-Host "Using Modrinth-compatible version '$collapsedPatch' for mod lookup (from '$GameVersion')." -ForegroundColor Yellow
            return $collapsedPatch
        }
    }

    if ($GameVersion -match '^(\d+\.\d+)') {
        $prefix = "$($Matches[1])."
        $fallback = $available | Where-Object { $_.StartsWith($prefix) } | Sort-Object -Descending | Select-Object -First 1
        if ($fallback) {
            Write-Host "Using nearest Modrinth game version '$fallback' for mod lookup (from '$GameVersion')." -ForegroundColor Yellow
            return $fallback
        }
    }

    Write-Host "No close Modrinth version found for '$GameVersion'; using original value." -ForegroundColor Yellow
    return $GameVersion
}

function Resolve-FabricApiVersion($GameVersion, $FallbackVersion) {
    $encodedGameVersions = [uri]::EscapeDataString("[\"$GameVersion\"]")
    $encodedLoaders = [uri]::EscapeDataString("[\"fabric\"]")
    $url = "https://api.modrinth.com/v2/project/fabric-api/version?game_versions=$encodedGameVersions&loaders=$encodedLoaders"
    try {
        $versions = Invoke-RestMethod -Headers @{ "User-Agent" = "JumpBoostBar/dev-runner" } -Uri $url -ErrorAction Stop
    } catch {
        Write-Host "Could not resolve Fabric API for $GameVersion, using configured fallback '$FallbackVersion'." -ForegroundColor Yellow
        return $FallbackVersion
    }

    if (-not $versions -or $versions.Count -eq 0) {
        Write-Host "No Fabric API version found for $GameVersion, using configured fallback '$FallbackVersion'." -ForegroundColor Yellow
        return $FallbackVersion
    }

    $sorted = $versions | Sort-Object -Property date_published -Descending
    $release = $sorted | Where-Object { $_.version_type -eq 'release' } | Select-Object -First 1
    $best = if ($release) { $release } else { $sorted | Select-Object -First 1 }
    if (-not $best -or -not $best.version_number) {
        Write-Host "Fabric API metadata incomplete for $GameVersion, using fallback '$FallbackVersion'." -ForegroundColor Yellow
        return $FallbackVersion
    }

    return [string]$best.version_number
}

function Resolve-FabricLoaderVersion($GameVersion, $FallbackVersion) {
    $url = "https://meta.fabricmc.net/v2/versions/loader/$GameVersion"
    try {
        $entries = Invoke-RestMethod -Headers @{ "User-Agent" = "JumpBoostBar/dev-runner" } -Uri $url -ErrorAction Stop
    } catch {
        Write-Host "Could not resolve Fabric Loader for $GameVersion, using configured fallback '$FallbackVersion'." -ForegroundColor Yellow
        return $FallbackVersion
    }

    if (-not $entries -or $entries.Count -eq 0) {
        Write-Host "No Fabric Loader version found for $GameVersion, using configured fallback '$FallbackVersion'." -ForegroundColor Yellow
        return $FallbackVersion
    }

    $stable = $entries | Where-Object { $_.loader.stable -eq $true } | Select-Object -First 1
    $best = if ($stable) { $stable } else { $entries | Select-Object -First 1 }
    if (-not $best -or -not $best.loader.version) {
        Write-Host "Fabric Loader metadata incomplete for $GameVersion, using fallback '$FallbackVersion'." -ForegroundColor Yellow
        return $FallbackVersion
    }

    return [string]$best.loader.version
}

$defaultVersion = Read-GradleProperty "mc_version" "1.21.1"
$defaultFabricApiVersion = Read-GradleProperty "fabric_api_version" "0.110.0+1.21.1"
$defaultFabricLoaderVersion = Read-GradleProperty "fabric_loader_version" "0.16.10"

Write-Host ""
Write-Host "   Next dev runner" -ForegroundColor Cyan
Write-Host "   JumpBoostBar client test boot" -ForegroundColor DarkGray

Show-ProgressBar "Checking loader jars"
Write-Host "Checked jars" -ForegroundColor Green

$loader = Read-Choice "Launch loader: fabric or neoforge" @("fabric", "neoforge") "fabric"
$gameVersion = Read-Host "Game version [$defaultVersion]"
if ([string]::IsNullOrWhiteSpace($gameVersion)) {
    $gameVersion = $defaultVersion
}
$gameVersion = $gameVersion.Trim()
$modrinthGameVersion = Resolve-ModrinthGameVersion $gameVersion

if ($gameVersion -ne $defaultVersion) {
    Write-Host "Using non-default Minecraft version '$gameVersion'. Resolving compatible loader dependencies..." -ForegroundColor Yellow
}

$runDir = Join-Path $loader "run"
$modsDir = Join-Path $runDir "mods"
New-Item -ItemType Directory -Force -Path $modsDir | Out-Null

# Always start with a clean mod test directory to avoid cross-version crashes.
Get-ChildItem -Path $modsDir -File -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue

Write-Step "Optional Modrinth compatibility mods"
Write-Host "Enter Modrinth project slugs or IDs separated by commas, or press Enter to skip."
$modInput = Read-Host "Mods"
if (-not [string]::IsNullOrWhiteSpace($modInput)) {
    $projects = $modInput.Split(",") | ForEach-Object { $_.Trim() } | Where-Object { $_ }
    foreach ($project in $projects) {
        Write-Host "Checking $project..." -ForegroundColor DarkGray
        $file = Find-ModrinthFile $project $loader $modrinthGameVersion
        if (-not $file) {
            Write-Host "No $loader $modrinthGameVersion file found for $project" -ForegroundColor Yellow
            continue
        }

        $target = Join-Path $modsDir $file.filename
        if (Test-Path $target) {
            Write-Host "Already downloaded $($file.filename)" -ForegroundColor Green
            continue
        }

        Show-ProgressBar "Downloading $($file.filename)"
        Invoke-WebRequest -Headers @{ "User-Agent" = "JumpBoostBar/dev-runner" } -Uri $file.url -OutFile $target
    }
}

Write-Step "Launching $loader $gameVersion"
Write-Host "External test mods are in $modsDir" -ForegroundColor DarkGray

$previousMc = $env:ORG_GRADLE_PROJECT_mc_version
$previousFabricLoader = $env:ORG_GRADLE_PROJECT_fabric_loader_version
$previousFabricApi = $env:ORG_GRADLE_PROJECT_fabric_api_version

$env:ORG_GRADLE_PROJECT_mc_version = $gameVersion

$gradleArgs = @("--no-daemon", ":${loader}:runClient")
if ($loader -eq "fabric") {
    $resolvedFabricApiVersion = Resolve-FabricApiVersion $gameVersion $defaultFabricApiVersion
    $resolvedFabricLoaderVersion = Resolve-FabricLoaderVersion $gameVersion $defaultFabricLoaderVersion
    Write-Host "Fabric dependency resolution: loader=$resolvedFabricLoaderVersion, fabric-api=$resolvedFabricApiVersion" -ForegroundColor DarkGray
    $env:ORG_GRADLE_PROJECT_fabric_loader_version = $resolvedFabricLoaderVersion
    $env:ORG_GRADLE_PROJECT_fabric_api_version = $resolvedFabricApiVersion
}

& ".\gradlew.bat" @gradleArgs
$exitCode = $LASTEXITCODE

if ($null -eq $previousMc) {
    Remove-Item Env:ORG_GRADLE_PROJECT_mc_version -ErrorAction SilentlyContinue
} else {
    $env:ORG_GRADLE_PROJECT_mc_version = $previousMc
}

if ($null -eq $previousFabricLoader) {
    Remove-Item Env:ORG_GRADLE_PROJECT_fabric_loader_version -ErrorAction SilentlyContinue
} else {
    $env:ORG_GRADLE_PROJECT_fabric_loader_version = $previousFabricLoader
}

if ($null -eq $previousFabricApi) {
    Remove-Item Env:ORG_GRADLE_PROJECT_fabric_api_version -ErrorAction SilentlyContinue
} else {
    $env:ORG_GRADLE_PROJECT_fabric_api_version = $previousFabricApi
}

# Cleanup downloaded compatibility mods after the game exits so next run starts clean.
Get-ChildItem -Path $modsDir -File -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue

exit $exitCode
