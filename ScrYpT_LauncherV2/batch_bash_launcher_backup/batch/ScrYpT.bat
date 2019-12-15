cd %0\..\Application

call java -Dsun.java2d.ddscale=false -Dsun.java2d.noddraw=true -Dsun.java2d.uiScale=1 -Dfile.encoding=utf8 -Xmx256m -Xms128m -jar ScrYpT_Launcher.jar %1

if %ERRORLEVEL% GEQ 1 goto error

exit 0

:error

start cmd /k "cd && echo. && echo Cannot launch ScrYpT, an error occurred. Remember that java JVM interpreter is needed to run ScrYpT (Preredded version is 1.8) && echo. && pause && exit 0"