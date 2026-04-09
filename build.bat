@echo off
REM ===================================================================
REM build.bat — Compile and run the Spotify Music Recommendation System
REM ===================================================================
REM
REM Java path is set explicitly to:
REM   C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
REM
REM You still need JavaFX SDK (separate download).
REM See instructions below.
REM
REM Usage:
REM   build.bat          — Compile only
REM   build.bat run      — Compile and run
REM ===================================================================

setlocal EnableDelayedExpansion

REM ---- Java Path (explicit) ----
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
set JAVAC="!JAVA_HOME!\bin\javac.exe"
set JAVA="!JAVA_HOME!\bin\java.exe"

REM ---- JavaFX SDK Path ----
set JAVAFX_LIB=C:\javafx-sdk-26\lib

REM ---- Source and output ----
set SRC_DIR=src
set OUT_DIR=out
set MAIN_CLASS=spotify.Main

echo ============================================
echo   Spotify Music Recommendation System
echo   Build Script
echo ============================================
echo.

REM ---- Verify Java exists ----
if not exist !JAVAC! (
    echo [ERROR] javac not found at: !JAVAC!
    echo Check that JAVA_HOME is correct.
    pause
    exit /b 1
)

if not exist !JAVA! (
    echo [ERROR] java not found at: !JAVA!
    echo Check that JAVA_HOME is correct.
    pause
    exit /b 1
)

echo [INFO] Java: !JAVA!
!JAVA! -version
echo.

REM ---- Create output directory ----
if not exist %OUT_DIR% (
    mkdir %OUT_DIR%
)

REM ---- Clean previous build ----
echo [INFO] Cleaning previous build...
if exist %OUT_DIR%\spotify (
    rmdir /s /q %OUT_DIR%\spotify
)

REM ---- Copy non-Java resources to output ----
echo [INFO] Copying resources...
if exist %SRC_DIR%\spotify\view\terminal.css (
    mkdir %OUT_DIR%\spotify\view
    copy /Y %SRC_DIR%\spotify\view\terminal.css %OUT_DIR%\spotify\view\terminal.css >nul
    echo [INFO]   Copied terminal.css
)

REM ---- Discover all Java source files ----
echo [INFO] Discovering source files...
set SRC_FILES=
for /r %SRC_DIR% %%f in (*.java) do (
    set SRC_FILES=!SRC_FILES! "%%f"
)

if "!SRC_FILES!"=="" (
    echo [ERROR] No Java source files found in %SRC_DIR%.
    pause
    exit /b 1
)

echo [INFO] Found source files.
echo.

REM ---- Check JavaFX exists ----
if not exist "!JAVAFX_LIB!\javafx.controls.jar" (
    echo [ERROR] JavaFX SDK not found at: !JAVAFX_LIB!
    echo.
    echo You need to download and extract JavaFX SDK first:
    echo   1. Go to https://gluonhq.com/products/javafx/
    echo   2. Download "JavaFX SDK" for Windows
    echo   3. Extract the zip to C:\javafx-sdk-21.0.1\
    echo   4. Make sure this file exists:
    echo      C:\javafx-sdk-21.0.1\lib\javafx.controls.jar
    echo.
    echo If you extracted to a different folder, edit JAVAFX_LIB
    echo at the top of build.bat to point to the lib folder.
    echo.
    pause
    exit /b 1
)

echo [INFO] JavaFX found at: !JAVAFX_LIB!
echo.

REM ---- Compile ----
echo [INFO] Compiling...
echo.

%JAVAC% -d %OUT_DIR% --module-path "!JAVAFX_LIB!" --add-modules javafx.controls,javafx.fxml !SRC_FILES!

if !ERRORLEVEL! NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. See errors above.
    pause
    exit /b 1
)

echo.
echo ============================================
echo   BUILD SUCCESSFUL
echo   Output: %OUT_DIR%
echo ============================================
echo.

REM ---- Run if requested ----
if /i "%~1"=="run" (
    call :run_app
) else (
    echo To run the app:
    echo   build.bat run
)

endlocal
pause
exit /b 0

REM ---- Run Subroutine ----
:run_app
    echo [INFO] Starting application...
    echo.
    %JAVA% --module-path "!JAVAFX_LIB!" --add-modules javafx.controls,javafx.fxml -cp %OUT_DIR% %MAIN_CLASS%
    goto :eof
