# setup-android-sdk.ps1
# Android SDK Setup Script for Calendar-Driven Alarm App
# This script installs the required Android SDK components for building the project

Write-Host "=== Android SDK Setup for Alarmed Project ===" -ForegroundColor Green
Write-Host "This will install the necessary SDK components for Android development.`n" -ForegroundColor Yellow

# Set Android SDK root
$ANDROID_SDK_ROOT = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_HOME = $ANDROID_SDK_ROOT
$env:PATH += ";$ANDROID_SDK_ROOT\platform-tools;$ANDROID_SDK_ROOT\cmdline-tools\latest\bin"

Write-Host "Android SDK Root: $ANDROID_SDK_ROOT" -ForegroundColor Cyan

# Create SDK directory if it doesn't exist
if (-not (Test-Path $ANDROID_SDK_ROOT)) {
    New-Item -ItemType Directory -Path $ANDROID_SDK_ROOT -Force | Out-Null
    Write-Host "Created SDK directory" -ForegroundColor Green
}

# Check if command line tools are available
$SDK_MANAGER = "$ANDROID_SDK_ROOT\cmdline-tools\latest\bin\sdkmanager.bat"

if (-not (Test-Path $SDK_MANAGER)) {
    Write-Host "Command line tools not found. Installing Android SDK Command Line Tools..." -ForegroundColor Yellow
    
    # Download and install command line tools
    $CMD_TOOLS_URL = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
    $CMD_TOOLS_ZIP = "$env:TEMP\commandlinetools.zip"
    
    Write-Host "Downloading command line tools..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri $CMD_TOOLS_URL -OutFile $CMD_TOOLS_ZIP
    
    # Create cmdline-tools directory
    $CMD_TOOLS_DIR = "$ANDROID_SDK_ROOT\cmdline-tools"
    New-Item -ItemType Directory -Path $CMD_TOOLS_DIR -Force | Out-Null
    
    # Extract
    Write-Host "Extracting command line tools..." -ForegroundColor Cyan
    Expand-Archive -Path $CMD_TOOLS_ZIP -DestinationPath $CMD_TOOLS_DIR -Force
    
    # Move to latest directory structure
    Move-Item -Path "$CMD_TOOLS_DIR\cmdline-tools" -Destination "$CMD_TOOLS_DIR\latest" -Force
    
    Remove-Item $CMD_TOOLS_ZIP -Force
    Write-Host "Command line tools installed successfully!" -ForegroundColor Green
}

# Install required SDK components
Write-Host "`nInstalling Android SDK components..." -ForegroundColor Cyan
Write-Host "This may take several minutes..." -ForegroundColor Yellow

$SDK_COMPONENTS = @(
    "platform-tools",
    "platforms;android-35",
    "build-tools;35.0.0",
    "emulator"
)

foreach ($component in $SDK_COMPONENTS) {
    Write-Host "Installing $component..." -ForegroundColor Gray
    & $SDK_MANAGER --sdk_root=$ANDROID_SDK_ROOT $component --include_obsolete --no_https --accept-licenses
}

Write-Host "`n✅ Android SDK Setup Complete!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Run: .\gradlew.bat assembleDebug" -ForegroundColor White
Write-Host "2. Open the project in Android Studio" -ForegroundColor White
Write-Host "3. The project should now build successfully" -ForegroundColor White

Write-Host "`nEnvironment variables set:" -ForegroundColor Cyan
Write-Host "ANDROID_HOME=$env:ANDROID_HOME" -ForegroundColor White
Write-Host "JAVA_HOME=$env:JAVA_HOME" -ForegroundColor White
