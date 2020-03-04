set JDK_PATH=C:\Program Files\AdoptOpenJDK\jdk-8.0.232.09-hotspot/bin; 

set PATH=%JDK_PATH%%PATH% 

echo %PATH% 

java -Xmx768m -Xms348m -Xss100m  -Dsun.java2d.uiScale=1 -Djava.library.path="./Resources_ScreepT/lib" -cp "./*;./Resources_ScreepT/lib/*"  maingUI.MainGuiGeneralAppStarter 

exit 0