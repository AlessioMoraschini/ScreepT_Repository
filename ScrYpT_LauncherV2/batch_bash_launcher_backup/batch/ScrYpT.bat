cd Application

call java -Xms128m -Dsun.java2d.uiScale=1 -Djava.library.path="./Resources_Scrypt/lib" -Dfile.encoding=UTF8 -jar ScrYpT_Launcher.jar %1

if %ERRORLEVEL% GEQ 1 goto errormsg

exit 0

:errormsg 
start cmd /k "echo Java is needed to run scrypt, please install java virtual machine before to start (preferred verson is 1.8) & pause & exit 1"