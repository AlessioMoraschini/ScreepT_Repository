cd %0\..\Application

echo %CD%

java -version

if %ERRORLEVEL% GEQ 1 goto undefjava

call java -Dsun.java2d.ddscale=false -Dsun.java2d.noddraw=true -Dsun.java2d.uiScale=1 -Dfile.encoding=utf8 -Xmx256m -Xms128m -jar ScreepT_Launcher.jar %1

if %ERRORLEVEL% GEQ 1 goto error

exit 0

:error
start cmd /k "cd && echo. && echo Cannot launch ScreepT, an error occurred. && echo. && pause && exit 0"
exit 1

:undefjava
start cmd /k "cd && echo. && echo Cannot launch ScreepT, it seems that there is no java virtual machine installed on this system. Remember that java JVM interpreter is needed to run ScreepT (Preredded version is 1.8) && echo. && pause && exit 0"
exit 1