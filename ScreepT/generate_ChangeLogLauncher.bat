cd /d "%~dp0"
cd ..

git log 0.9.20.. --invert-grep --grep="EXCL_CHLOG" --tags --decorate=full --oneline --pretty=tformat:"%%ad %%d %%s%%n" --date=format:"%%Y-%%m-%%d" > changelog.log

set currentFile="%CD%\ScreepT\changelog.log"

@echo changeLog updated and exported to %currentFile%
pause