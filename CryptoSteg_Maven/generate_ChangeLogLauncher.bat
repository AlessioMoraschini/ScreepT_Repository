cd ..

git log v0.9.11.. --tags --decorate=full --oneline --graph --pretty=tformat:"%%ad %%d %%s%%n" --date=format:"%%Y-%%m-%%d" > CryptoSteg_Maven\changelog.log

set currentFile="%CD%\CryptoSteg_Maven\changelog.log"

@echo changeLog updated and exported to %currentFile%