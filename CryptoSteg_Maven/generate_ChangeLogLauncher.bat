set ROOT="C:\Users\xx\Desktop\Documenti&Utili\Informatica varie\SVN_Repository"

cd %ROOT%

git.exe git log v0.9.11.. --decorate --oneline --graph --pretty=tformat:"%ai %s%n" > CryptoSteg_Maven\changelog.log

pause