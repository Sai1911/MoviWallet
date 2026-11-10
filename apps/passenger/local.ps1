param(
    [ValidateSet('build', 'run', 'test')]
    [string]$Action = 'run'
)

$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not $env:JAVA_HOME) {
        $moviJdk = Get-ChildItem -Path "$env:ProgramFiles\Android\Android Studio*\jbr" -Directory -ErrorAction SilentlyContinue |
            Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if (-not $moviJdk) { throw 'Configura JAVA_HOME con el JDK de Android Studio.' }
        $env:JAVA_HOME = $moviJdk.FullName
    }
    if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $moviAdb = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
    if (-not (Test-Path $moviAdb)) { throw 'Configura ANDROID_HOME con la carpeta de tu Android SDK.' }
    $moviTasks = switch ($Action) {
        'build' { @(':app:assembleDebug') }
        'test' { @(':app:testDebugUnitTest', ':app:lintDebug') }
        'run' { @(':app:assembleDebug') }
    }
    & .\gradlew.bat @moviTasks --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Gradle terminó con errores. Revisa la salida anterior.' }
    if ($Action -eq 'run') {
        $moviDevices = @(& $moviAdb devices | Select-String '^\S+\s+device$')
        if ($moviDevices.Count -ne 1) { throw 'Abre un emulador o conecta un teléfono autorizado. Debe haber exactamente un dispositivo listo.' }
        $moviSerial = ($moviDevices[0].Line -split '\s+')[0]
        & $moviAdb -s $moviSerial install -r 'app\build\outputs\apk\debug\app-debug.apk'
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo instalar el APK.' }
        & $moviAdb -s $moviSerial shell am start -n 'pe.moviwallet.passenger/.MainActivity'
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo abrir la app.' }
    }
} finally {
    Pop-Location
}
