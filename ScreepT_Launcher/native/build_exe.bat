@echo off
rem Builds ScreepT.exe (32 bit) and ScreepT_64.exe (64 bit) with the C# compiler included in Windows (.NET Framework 4).
rem Not part of the Maven build: run it only when ScreepT.cs changes, then commit the two exe.
rem Output goes to ..\..\ScreepT\ROOT_DIR_APP_FILES (the folder copied in every release).
setlocal
set CSC=%WINDIR%\Microsoft.NET\Framework64\v4.0.30319\csc.exe
set OUT=%~dp0..\..\ScreepT\ROOT_DIR_APP_FILES
set COMMON=/nologo /target:winexe /optimize+ /debug- /win32icon:"%~dp0ScreepT.ico" /win32manifest:"%~dp0ScreepT.manifest" /r:System.Windows.Forms.dll /r:System.Drawing.dll /r:System.IO.Compression.FileSystem.dll /r:System.IO.Compression.dll
"%CSC%" %COMMON% /platform:x86 /out:"%OUT%\ScreepT.exe" "%~dp0ScreepT.cs" || exit /b 1
"%CSC%" %COMMON% /platform:x64 /out:"%OUT%\ScreepT_64.exe" "%~dp0ScreepT.cs" || exit /b 1
echo Built in %OUT%
