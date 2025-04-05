# Schnellstart

### Start der Anwendung von der Kommandozeile

`mvn javafx:run`

### Projekt aufräumen

`mvn clean`

### Übersetzen der Anwendung

`mvn package`

### Bau des Image zur Weitergabe oder zur weiteren Verwendung für den Baus eines msi-Pakets

`mvn -Dimage install`

### Komplettes Übersetzen der Anwendung inkl. Dokumentation

`mvn -Dimage clean install site`

### Suche nach Fehlern im Javacode (Start aus dem Terminal)

`mvn spotbugs:gui`

