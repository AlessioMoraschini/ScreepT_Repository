#!/bin/bash

find ./Application  -type f -exec chmod 755 {} \;

pwd
cd Application/
pwd

java -Xms128m -Dsun.java2d.uiScale=1 -Djava.library.path="./Resources_ScreepT/lib" -Dfile.encoding=UTF8 -jar ScreepT_Launcher.jar $1

pwd
