set JDK_PATH=C:\Program Files\java/jdk-11.0.2/bin; 

set PATH=%JDK_PATH%%PATH% 

echo %PATH% 

java -Xmx512m -Xms256m -Xss125m -XX:MaxPermSize=128m -XshowSettings:vm -Dfile.encoding=UTF8 -Dawt.useSystemAAFontSettings=on -Dsun.java2d.d3d=false -Dsun.java2d.ddscale=true -Dsun.java2d.uiScale=1 -Djava.library.path="./Resources_Scrypt/lib" -jar ScrYpT_0.7.6.jar 

exit 0