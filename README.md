# Spotify Lyrics (Fabric, Minecraft 1.21.11)

Zeigt die Lyrics des gerade laufenden Spotify-Songs als schwebende Schrift im Spiel.

## JAR bauen
Projekt in ein GitHub-Repo hochladen. Unter "Actions" läuft "build",
dort das Artifact "spotify-lyrics-jar" herunterladen.

## Installieren
JAR (nicht die -sources.jar) nach .minecraft/mods/ kopieren.
Benötigt: Fabric Loader + Fabric API für Minecraft 1.21.11.

## Spotify einrichten
1. developer.spotify.com/dashboard -> App erstellen, Web API aktivieren
2. Redirect URI: http://127.0.0.1:8888/callback
3. Im Spiel Taste K drücken, Client ID einfügen, "Mit Spotify verbinden" klicken.

Alle Einstellungen (an/aus, Anzeige, Farbe, Größe, Timing) sind im Menü auf Taste K.
