@echo off
rem ScreepT starter (Windows). Finds Java 8 or newer; if missing, offers to download a Java JRE into the "jre" folder next to this file.
rem To test the download without removing Java: set SCREEPT_FORCE_NO_JAVA=1 (then delete the "jre" folder to repeat).
setlocal EnableExtensions
set "SR=%~dp0"
cd /d "%SR%Application"
if not exist ScreepT_Launcher.jar goto nojar

set "JAVA_EXE="
call :check "%SR%jre\bin\java.exe"
if defined JAVA_EXE goto run
if "%SCREEPT_FORCE_NO_JAVA%"=="1" goto nojava
if defined JAVA_HOME call :check "%JAVA_HOME%\bin\java.exe"
if defined JAVA_EXE goto run
for /f "delims=" %%p in ('where java.exe 2^>nul') do if not defined JAVA_EXE call :check "%%p"
if defined JAVA_EXE goto run
goto nojava

:run
for %%i in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpi"
for %%i in ("%JAVA_BIN%..") do set "JAVA_HOME=%%~fi"
rem the Java chosen here is also the one the ScreepT launcher finds in PATH
set "PATH=%JAVA_BIN%;%PATH%"
call "%JAVA_EXE%" -Dsun.java2d.ddscale=false -Dsun.java2d.noddraw=true -Dsun.java2d.uiScale=1 -Dfile.encoding=utf8 -Xmx256m -Xms128m -jar ScreepT_Launcher.jar %*
if errorlevel 1 goto error
exit /b 0

:check
rem sets JAVA_EXE to %1 when it is a Java 8 or newer
set "JV="
set "JA="
set "JB="
set "JM="
if not exist "%~1" exit /b 1
"%~1" -version 2>"%TEMP%\screept_jv.txt"
for /f "tokens=3" %%v in ('findstr /i /c:"version" "%TEMP%\screept_jv.txt"') do if not defined JV set "JV=%%~v"
del "%TEMP%\screept_jv.txt" 2>nul
if not defined JV exit /b 1
for /f "tokens=1,2 delims=._-" %%a in ("%JV%") do set "JA=%%a" & set "JB=%%b"
set "JM=%JA%"
if "%JA%"=="1" set "JM=%JB%"
if not defined JM exit /b 1
if %JM% LSS 8 exit /b 1
set "JAVA_EXE=%~1"
exit /b 0

:nojava
echo.
echo ScreepT needs Java 8 or newer, but no compatible Java was found on this PC.
echo Java 21 (Eclipse Temurin, free, about 50 MB) can be downloaded into the ScreepT folder:
echo %SR%jre
echo It is not installed system wide and needs no administrator rights.
echo.
choice /c YN /m "Download it now"
if errorlevel 2 exit /b 1
echo.
echo Downloading, please wait...
powershell -NoProfile -ExecutionPolicy Bypass -File "%SR%ScreepT_JavaSetup.ps1" -Root "%SR:~0,-1%"
if errorlevel 1 goto dlfail
call :check "%SR%jre\bin\java.exe"
if defined JAVA_EXE goto run

:dlfail
echo.
echo Cannot download Java automatically. Install Java 8 or newer manually
echo (for example Eclipse Temurin: https://adoptium.net) and start ScreepT again.
pause
exit /b 1

:nojar
echo.
echo Cannot find ScreepT_Launcher.jar in "%SR%Application".
pause
exit /b 1

:error
start cmd /k "cd && echo. && echo Cannot launch ScreepT, an error occurred. && echo. && pause && exit 0"
exit /b 1
