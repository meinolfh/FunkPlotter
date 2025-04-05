# Um ein bestehendes, lokales Java-Repository nach GitHub zu deployen, folge diesen Schritten:

### Repository auf GitHub erstellen
   Gehe zu GitHub und melde dich an.
   Klicke auf das "+" oben rechts und wähle "New repository".
   Gib einen Namen für dein Repository ein und wähle öffentlich oder privat.
   Lasse „Initialize this repository with a README“ deaktiviert, wenn du dein lokales Repository pushen möchtest.
   Klicke auf "Create repository".

### Lokales Repository initialisieren (falls nicht bereits geschehen)
   Falls dein Java-Projekt noch kein Git-Repository ist, öffne ein Terminal oder eine Eingabeaufforderung
   und navigiere zu deinem Projektordner:
   cd /pfad/zu/deinem/projekt
   git init
   Falls bereits ein .git-Verzeichnis existiert, kannst du diesen Schritt überspringen.

### Remote-Repository hinzufügen
   Verbinde dein lokales Repository mit GitHub:
   git remote add origin https://github.com/GITHUB-NAME/REPO-NAME.git

### Überprüfe die Verbindung mit:
`git remote -v`

### Dateien hinzufügen und committen
   Falls noch nicht geschehen, füge deine Dateien hinzu:

`git add .`

`git commit -m "Initial commit"`

   Falls du eine Datei .gitignore für Java-Projekte benötigst, kannst du eine mit einem Editor erstellen

### Code auf GitHub pushen
   Falls dein Repository auf GitHub leer ist, verwende:
   Falls dein Standardbranch nicht "main" ist:

`git branch -M main`

`git push -u origin main`

   Falls bereits Commits auf GitHub existieren und du dein lokales Repository anpassen möchtest,
   hole zuerst die neuesten Änderungen:

`git pull origin main --rebase`

`git push origin main`

### Überprüfung auf GitHub
   Gehe zu deinem GitHub-Repository und überprüfe, ob die Dateien hochgeladen wurden.