@echo off
echo Building AirMouse Server JAR...

set MVN_CMD="C:\Users\fuzzy\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"
call %MVN_CMD% clean package -q -DskipTests
if %errorlevel% neq 0 (
    echo Maven build failed.
    exit /b %errorlevel%
)

echo Building Windows Executable using jpackage...

REM Define paths
set APP_NAME="AirMouse Server"
set APP_VERSION=1.0.0
set JAR_FILE=target\airmouse-server-1.0.0-RC1.jar
set MAIN_CLASS=com.airmouse.server.Main
set ICON_FILE=src\main\resources\icons\AirMouse.png

REM Ensure target output dir exists
if not exist "dist" mkdir dist

REM Check if WiX is installed by looking for candle.exe in PATH
where candle >nul 2>nul
if %errorlevel% equ 0 (
    echo WiX toolset found. Building .exe installer...
    jpackage ^
      --type exe ^
      --dest dist ^
      --input target ^
      --main-jar airmouse-server-1.0.0-RC1.jar ^
      --main-class %MAIN_CLASS% ^
      --name %APP_NAME% ^
      --app-version %APP_VERSION% ^
      --icon %ICON_FILE% ^
      --win-shortcut ^
      --win-menu ^
      --win-menu-group "AirMouse" ^
      --java-options "-Xms32m -Xmx128m"
      
    echo Build complete! Installer is in the 'dist' folder.
) else (
    echo WiX toolset not found. Building standalone app-image directory instead...
    echo ^(Install WiX Toolset v3 to build .exe installers^)
    
    jpackage ^
      --type app-image ^
      --dest dist ^
      --input target ^
      --main-jar airmouse-server-1.0.0-RC1.jar ^
      --main-class %MAIN_CLASS% ^
      --name %APP_NAME% ^
      --app-version %APP_VERSION% ^
      --icon %ICON_FILE% ^
      --java-options "-Xms32m -Xmx128m"
      
    echo Build complete! App directory is in the 'dist' folder.
)
