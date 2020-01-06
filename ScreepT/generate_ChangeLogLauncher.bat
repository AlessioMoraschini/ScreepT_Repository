cd ..

git log v0.9.11.. --invert-grep --grep=EXCL_CHLOG --tags --decorate=full --oneline --graph --pretty=tformat:"%%ad %%d %%s%%n" --date=format:"%%Y-%%m-%%d" > ScreepT\changelog.log

set currentFile="%CD%\ScreepT\changelog.log"

@echo changeLog updated and exported to %currentFile%