@echo off
rem ScreepT search in files from the command line: the same engine as Search in files, at full speed and without windows.
rem Usage: screept-search [options] TEXT [PATH...]   (screept-search --help for the options)
setlocal
set "SR=%~dp0"
set "JAVA_EXE="
if exist "%SR%jre\bin\java.exe" set "JAVA_EXE=%SR%jre\bin\java.exe"
if not defined JAVA_EXE if exist "%SR%Application\jre\bin\java.exe" set "JAVA_EXE=%SR%Application\jre\bin\java.exe"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for /f "delims=" %%p in ('where java.exe 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%p"
if not defined JAVA_EXE (
	echo screept-search: Java 8 or newer is needed: start ScreepT.bat once to get it. 1>&2
	exit /b 2
)
set "JAR="
for %%j in ("%SR%Application\ScreepT_*.jar") do if /i not "%%~nj"=="ScreepT_Launcher" set "JAR=%%~fj"
if not defined JAR (
	echo screept-search: ScreepT jar not found in "%SR%Application" 1>&2
	exit /b 2
)
rem UTF-8 console while the search prints, then the previous code page
set "OLDCP="
for /f "tokens=2 delims=:." %%c in ('chcp') do set "OLDCP=%%c"
chcp 65001 >nul
"%JAVA_EXE%" -Dfile.encoding=UTF-8 -XX:+UseParallelGC -cp "%JAR%;%SR%Application\Resources_ScreepT\lib\*" gui.texteditor.commons.filesearch.cli.SearchCli %*
set "RC=%ERRORLEVEL%"
if defined OLDCP chcp %OLDCP% >nul
exit /b %RC%
