$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $repoRoot "src"
$outputRoot = Join-Path $repoRoot "build\classes"

New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null
$sourceFiles = Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter "*.java" |
    Select-Object -ExpandProperty FullName

if (-not $sourceFiles) {
    throw "No Java source files were found under $sourceRoot"
}

javac --release 17 -encoding UTF-8 -d $outputRoot $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed with exit code $LASTEXITCODE"
}

Write-Output "Compiled $($sourceFiles.Count) Java files into $outputRoot"
