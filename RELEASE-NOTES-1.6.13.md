# ScootPit BMS 1.6.13

<img src="https://raw.githubusercontent.com/Tim-Gurke/ScootPit-BMS/v1.6.13/docs/assets/screenshots/cockpit-version-1.6.13.png" width="360" alt="ScootPit BMS 1.6.13 mit Versionsnummer im Appkopf">

*Echte Appaufnahme unter Android 13 im Emulator mit dem Profil „Beispiel-Scooter“ und Beispielwerten.*

- **Verbrauch ab Reset nach Fahrtpausen korrigiert:** Ausgeschlossene Pausen ohne Strecken-, Fahrzeit- oder Energiezuwachs erzeugen bei fehlenden BMS-Paketen keine künstliche Energielücke mehr. Auch Fortsetzen und Beenden aus einer Pause erhalten den Zählerstand.
- **Unvollständige Verbrauchswerte verständlich anzeigen:** Bereits gespeicherte Reset-Zähler bleiben erhalten. Bei alten oder tatsächlichen Messlücken zeigt **≈** die erfasste Energie je aufgezeichneter Strecke. Fehlende Energie wird nicht ergänzt; der Wert kann den tatsächlichen Verbrauch unterschätzen. Für den ersten Test nichts zurücksetzen.
- **Lokale Kopplung mit Watt’n’Pit:** Beide offiziellen Apps können auf demselben Handy aktuelle BMS-Grunddaten gemeinsam nutzen. Die Bluetooth-Verbindung wird abgestimmt; ScootPit erhält Vorrang für die Fahrtaufzeichnung. GPS-Daten und Fahrtrouten werden nicht geteilt. Watt’n’Pit ab 1.5.0 und dasselbe ausgewählte BMS sind erforderlich.
- **Version im Appkopf:** Die installierte Versionsnummer steht direkt neben dem Appnamen. Appversion und **© 2026 Tim Weißbach und Mitwirkende** stehen zusätzlich dezent am Ende der Einstellungen und der Copyright-Vermerk im Lizenzbereich der README.

[Anleitung zur Kopplung und zum Praxistest](https://github.com/Tim-Gurke/ScootPit-BMS/blob/v1.6.13/docs/watt-n-pit.md) · [Durchschnittswerte bei Fahrtpausen](https://github.com/Tim-Gurke/ScootPit-BMS/blob/v1.6.13/docs/cockpit.md#durchschnittswerte-bei-fahrtpausen-ab-1613)

**Praxistest am Roller steht aus:** Bluetooth-Übergabe sowie Pause und Fortsetzen mit dem eigenen Gerät prüfen. Die BMS-Abschaltprüfung bleibt bei fünf Sekunden geringer Stromaufnahme und erkennt einen eingeschalteten Roller nicht zuverlässig; zum Sperren den Roller vorher selbst ausschalten.

Die signierte APK als Update über die vorhandene App installieren. Profile, Kilometerzähler, Layouts und Fahrten bleiben erhalten.
