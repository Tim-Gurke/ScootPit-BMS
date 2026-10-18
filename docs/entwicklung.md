# Entwicklung und dauerhafte Signierung

[← Übersicht](../README.md)

## Dauerhafte APK-Signatur

Für dieses Projekt ist ein dauerhafter Release-Schlüssel eingerichtet. Die private Schlüsselsicherung separat und sicher aufbewahren. Für alle kommenden Updates denselben Schlüssel verwenden; ein neuer Schlüssel würde die Installation als Update verhindern. Niemals Keystore oder Passwörter ins Repository oder als öffentliches Build-Artefakt legen. Unter **Settings → Secrets and variables → Actions** sind vier Repository-Secrets hinterlegt:

| Secret | Inhalt |
| --- | --- |
| `APP_KEYSTORE_BASE64` | Base64-Inhalt des eigenen Keystores |
| `APP_KEYSTORE_PASSWORD` | Keystore-Passwort |
| `APP_KEY_ALIAS` | Alias des Release-Schlüssels |
| `APP_KEY_PASSWORD` | Passwort dieses Schlüssels |

Base64 ist lediglich ein Transportformat. Der Workflow schreibt den Keystore nur vorübergehend auf den Runner, baut die signierte Release-APK und entfernt die Datei wieder. Für offizielle Veröffentlichungen müssen alle vier Secrets eingerichtet sein; fehlende oder unvollständige Signierung führt zu einem Fehler. Das Artefakt enthält den Signiermodus in `signing-mode.txt`. Release-APKs werden vor Veröffentlichung mit der bekannten Zertifikat-SHA-256 von 1.1.1 verglichen: `c58abf26de59344a21efbc0576e391a36b6179724a0b4fad27a35de03de184fb`. Eine anders signierte Debug-APK ist kein kompatibles Update.

## Offizielle Version

Die öffentliche Version heißt **1.6.10**. Android verwendet intern **versionCode 42**, damit das Update über bereits installierte Versionen funktioniert. Die Versionsbezeichnung kann öffentlich neu beginnen; der interne Code muss bei jedem Update weiter steigen. App-ID und Signierschlüssel bleiben erhalten.

Build: Java 17, Gradle 8.9, Android SDK 35. Die Java-Tests decken Protokoll, Strom-Skalierung und sichere Lastausgangssteuerung, Standstrom-Lernen, manuelle Fahrtpause mit automatischer Fortsetzung, Reichweite, Raster, Empfang, bestätigten Stillstand, Skalenfarbverläufe, Kontrast inaktiver Skalen im dunklen Design, umgekehrte Skalenrichtung, die Verbrauchsumrechnung pro 10 km, Durchschnittsfenster bis 100000 m, aktuelle Fahrt, unabhängige persistente Reset-Zähler und BMS-Messlücken, Statusposition im Querformatkopf, übernommene Layouts und scrollbare Untermenüs ab; Android Lint und Emulatorprüfungen unter Android 13/15 ergänzen die Prüfung. Die Offline-Höhenkorrektur wird zusätzlich unter Android 8 geprüft. Reales BLE/GPS und Samsung-Routinen müssen am eigenen Roller geprüft werden.



## Screenshots der Bedienungsanleitung

Der UI-Workflow erzeugt über den Modus `guide` des Android-Testläufers und die Aufnahmeroutine `GuideScreenshots` echte Appaufnahmen mit Beispielwerten. Die Beispiele bleiben ausschließlich im Test-APK; die ausgelieferte App enthält keinen simulierten BMS-Datenmodus. Geprüft werden außerdem die Übernahme des neuen Standardlayouts, der Erhalt persönlicher Anordnungen und ein Symbolkontrast von mindestens 4,5:1 im Pausenknopf beider Designs. Screenshots liegen im UI-Artefakt; geprüfte Aufnahmen sind unter `docs/assets/screenshots/anleitung-*.png` eingebunden.

## Höhenbezug und Verbindungsdiagnose in 1.6.10

`AltitudeReference` nutzt `androidx.core:core-location-altitude:1.0.0`. Die Bibliothek liefert die lokale Umrechnung von WGS84-Ellipsoid zu MSL und unterstützt ältere Android-Versionen. Die Berechnung läuft auf einem seriellen Worker; eintreffende Ergebnisse werden nur an dieselbe noch offene Fahrt übergeben. Der ursprüngliche `Location`-Höhenwert bleibt erhalten. Bei fehlgeschlagener Umrechnung wird keine rohe Höhe als MSL ausgegeben.

Aufzeichnungsdateien tragen ihren Höhenbezug; Import erhält ihn. `AltitudeCheck` prüft mit deaktiviertem Netzwerk positive und negative Geoidkorrekturen, fehlende Höhen, unveränderte Rohwerte und beide Export-/Importwege. Bestandsdateien bleiben erhalten. `ConnectionTraceTest` prüft die begrenzte Ereigniszahl, relative monotone Zeiten und das Zurücksetzen der Sitzung. Die Android-Prüfung öffnet außerdem den Diagnose-Dialog und erstellt eine Aufnahme mit einem ausdrücklich markierten Beispielablauf.

Release-Zweige werden vor der Übernahme nach `main` gebaut und geprüft. Nur `main` veröffentlicht ein Release; es bleiben die drei zuletzt veröffentlichten Releases verfügbar.
