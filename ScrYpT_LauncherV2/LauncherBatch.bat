set JDK_PATH=C:\Program Files\AdoptOpenJDK\jdk-8.0.232.09-hotspot/bin; 

set PATH=%JDK_PATH%%PATH% 

echo %PATH% 

javaw -Xmx768m -Xms348m -Xss100m  -Dsun.java2d.uiScale=1 -Djava.library.path="./Resources_Scrypt/lib" -cp ./*;./Resources_Scrypt/lib/*  maingUI.MainGuiGeneralAppStarter 

exit 0