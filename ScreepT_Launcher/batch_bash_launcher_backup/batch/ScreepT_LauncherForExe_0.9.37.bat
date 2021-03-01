::[Bat To Exe Converter]
::
::YAwzoRdxOk+EWAjk
::fBw5plQjdCyDJGyX8VAjFANMcCCHL2CuCaUgzOf4+ueCrFkUEtcrcYPS5p2AJu8B50TwfJUslmxbisIJMChbbBeufjsLm2tSuXeMI8iOqjPSSVqM41sEJEl1km3ThC4pXNFpjsAL7CK/70XAk6QF33v3W7sxF2Liyrl4BMsB6Qm8VEDLlb0bQ/XhCw==
::YAwzuBVtJxjWCl3EqQJgSA==
::ZR4luwNxJguZRRnk
::Yhs/ulQjdF+5
::cxAkpRVqdFKZSzk=
::cBs/ulQjdF+5
::ZR41oxFsdFKZSDk=
::eBoioBt6dFKZSDk=
::cRo6pxp7LAbNWATEpCI=
::egkzugNsPRvcWATEpCI=
::dAsiuh18IRvcCxnZtBJQ
::cRYluBh/LU+EWAnk
::YxY4rhs+aU+IeA==
::cxY6rQJ7JhzQF1fEqQJhZkoaHErSXA==
::ZQ05rAF9IBncCkqN+0xwdVsEAl3MbzLa
::ZQ05rAF9IAHYFVzEqQIDKwlRSRS2XA==
::eg0/rx1wNQPfEVWB+kM9LVsJDGQ=
::fBEirQZwNQPfEVWB+kM9LVsJDGQ=
::cRolqwZ3JBvQF1fEqQJQ
::dhA7uBVwLU+EWHjK2QxwDB5HRQOMfEq0AvU47f7+4+WXr0gTQII=
::YQ03rBFzNR3SWATElA==
::dhAmsQZ3MwfNWATE1U41OwhdQ0SvM3m7FbYU4ebyjw==
::ZQ0/vhVqMQ3MEVWAtB9wSA==
::Zg8zqx1/OA3MEVWAtB9wSA==
::dhA7pRFwIByZRRmh+kg/MVsPBWQ=
::Zh4grVQjdCyDJGyX8VAjFANMcCCHL2CuCaUgzOf4+ueCrFkUEtcrcYPS5p2AJu8B50TwfJUslmxbisIJMChbbBeufjsLm2tSuXeMI8iOqjPSSVqM41sEJEl1km3ThC4pXNFpjsAL7CK/70XAk6QF33v3W7sxF2Liyrl4BMwY+DaHa1vNn6NhddrvfrPqCzPaBWYNqWjGtNMkx81+Bh8jGhpTn/dp+S6Y
::YB416Ek+ZG8=
::
::
::978f952a14a936cc963da21a135fa983
cd %0\..\Application

echo %CD%

java -version

if %ERRORLEVEL% GEQ 1 goto undefjava

call java -Dsun.java2d.ddscale=false -Dsun.java2d.noddraw=true -Dsun.java2d.uiScale=1 -Dfile.encoding=utf8 -Xmx128m -Xms128m -jar ScreepT_Launcher.jar %1

if %ERRORLEVEL% GEQ 1 goto error

exit 0

:error
start cmd /k "cd && echo. && echo Cannot launch ScreepT, an error occurred. && echo. && pause && exit 0"
exit 1

:undefjava
start cmd /k "cd && echo. && echo Cannot launch ScreepT, it seems that there is no java virtual machine installed on this system. Remember that java JVM interpreter is needed to run ScreepT (Preredded version is 1.8) && echo. && pause && exit 0"
exit 1