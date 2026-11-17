# Installation und Ersteinrichtung

[← Übersicht](../README.md) · [Bedienungsanleitung mit Bildern](anleitung.md)

## Build, Tests und Installation

GitHub Actions prüft die App und baut mit den eingerichteten Signier-Secrets eine Release-APK. Fehlende Signierung oder ein abweichendes Zertifikat brechen den offiziellen Build ab. Artefakte: **ScootPit-BMS-APK** und **ScootPit-BMS-UI**. Erforderlich: Java 17, Gradle 8.9, Android SDK 35; minSdk 26. Protokoll-, Steuerungs-, Reichweiten- und Rastertests laufen als Java-Tests; Android Lint prüft die App. Android-13/15-Emulatortests prüfen Messwerte, Beschriftungen, Layoutänderungen, Temperatur-Startschätzung, Log, persistente Ordnerberechtigungen, Settings-Roundtrip, fehlgeschlagene Fahrtkopie mit Wiederholung und Bereitschaft bei ausgeschaltetem Bildschirm. Bildschirmaufnahmen werden archiviert.

Die App-ID bleibt **de.wortmonster.jbdtrigger**, damit Routines und der vorhandene App-Datenbereich bei kompatiblen Updates erhalten bleiben. Die APK benötigt für ein Update dieselbe Signatur. Unterschiedliche Debug-Schlüssel verschiedener CI-Läufe können eine Neuinstallation erfordern. **Vor einer Deinstallation Fahrten und Einstellungen sichern; Deinstallation löscht interne Daten und Ordnerberechtigungen.** Für die offizielle APK ist der dauerhafte Release-Schlüssel über GitHub Actions Secrets eingerichtet. Beim Wechsel von einer früheren Testsignatur kann einmalig eine Neuinstallation erforderlich sein. Danach können neue Versionen mit demselben Schlüssel als Update installiert werden.

ZIP entpacken und APK öffnen. Die Einrichtungsübersicht führt durch die Freigaben, BMS-Auswahl und den Bereitschaftsstart. Reale BMS-Steuerung, Sensorzuordnung, GPS, Routine-Auslösung und Hintergrundbetrieb sind am Roller zu prüfen.

## Geführte Freigaben beim ersten Start

Bei einer frischen oder unvollständig eingerichteten Installation erscheint **ScootPit einrichten**. Jede Freigabe wird erklärt und erst nach Antippen der zugehörigen Schaltfläche bei Android angefordert. **Schritt 1 bis 10** führt jeweils zum nächsten offenen Punkt. Bereits erteilte Freigaben werden übersprungen; importierte Profile können keine Android-Berechtigungen erteilen. Vollständig eingerichtete Bestandsinstallationen erhalten keinen zusätzlichen Startdialog.

| Schritt | Einstellung und Zweck |
| --- | --- |
| Geräte in der Nähe | Ab Android 12 die Bluetooth-Suche und Verbindung erlauben. Bis Android 11 benötigt die Suche die präzise Standortfreigabe. |
| Bluetooth | Bluetooth am Handy einschalten. Das allein ersetzt keine App-Berechtigung. |
| Präziser Standort | Während der App-Nutzung erlauben und **Präzise** wählen, für Geschwindigkeit, Fahrterkennung und Fahrtroute. |
| Standort am Handy | Den Standortdienst für GPS-Aufzeichnungen einschalten. |
| Benachrichtigungen | Ab Android 13 empfohlen für Bereitschaft, Fahrtstatus und Routine-Nachrichten; keine Voraussetzung für die Bluetooth-Verbindung. |
| Akku-Hintergrundbetrieb | Für zuverlässige Erkennung bei gesperrtem Handy empfohlen. Samsung: zusätzlich in **App-Info → Akku → Nicht eingeschränkt** prüfen. |
| BMS auswählen | Zum Roller gehen, andere verbundene BMS-Apps trennen und das eigene BMS auswählen. |
| Bereitschaft starten | Wird verfügbar, sobald die erforderlichen Freigaben, Bluetooth, Standortdienst und eine BMS-Zuordnung vorhanden sind. |
| Aktuelle BMS-Daten | Auf gültige frische Daten warten. Andere BMS-Apps, das bisherige Handy oder die Zentrale gegebenenfalls trennen; bei Problemen direkt die Diagnose öffnen. |
| Kurze Testfahrt | Handy vor dem Losfahren verstauen. Die Automatik muss eine Fahrt erkennen und bei GPS-Empfang mindestens zwei Fahrtpunkte mit erkannter Strecke lokal schreiben. |

**Vorerst überspringen** lässt Benachrichtigungen und Akku-Ausnahme bewusst offen. Nach der BMS-Auswahl oder einem Abbruch der Auswahl kehrt die App automatisch zur passenden Einrichtungsseite zurück.

**Aufzeichnung geprüft** erscheint erst nach bestätigten Schreibvorgängen für mindestens zwei Fahrtpunkte mit einer erkannten Strecke und frischen BMS-/GPS-Daten des ausgewählten Scooters. Eine bloße Bluetooth-Verbindung, Bereitschaft, ein geöffneter Recorder oder ein einzelner Punkt reichen nicht. Speicherfehler, ausbleibende GPS-Daten und ein ausgeschalteter BMS-Lastausgang werden erklärt. Die App schaltet den Lastausgang dabei nicht automatisch. Der Nachweis wird auch bei geschlossenem Cockpit vom Aufzeichnungsdienst gespeichert und bleibt nach dem Abstellen erhalten; er wird dann als abgeschlossene Testaufzeichnung bezeichnet. Dies bestätigt die lokale Aufzeichnung; externe Ordnerkopien und gerätespezifischer Hintergrundbetrieb bleiben gesondert zu prüfen.

**Zum Cockpit** lässt die Einrichtung auf Wunsch offen. **Einrichtung fortsetzen · Testfahrt** erinnert an einen noch nicht abgeschlossenen Test. Die Übersicht bleibt unter **Einstellungen → Einrichtung und Berechtigungen** erreichbar. Bei fehlenden Voraussetzungen zeigt das Cockpit **Einrichtung prüfen**. Die BMS-Suche öffnet bei fehlender Bluetooth-Freigabe die Einrichtung; ein Bereitschaftsstart mit fehlenden Voraussetzungen ebenfalls. Nach Rückkehr aus den Android-Einstellungen werden die Zustände neu geprüft. Hat Android weitere Berechtigungsabfragen gesperrt, bietet die App einen direkten Weg zur App-Info samt passendem Einstellungspfad. Bestehende Kopplungen, Fahrten und Layouts bleiben erhalten.

Grundlagen: [Android-Bluetooth-Berechtigungen](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions), [Berechtigungen erklären und anfordern](https://developer.android.com/training/permissions/requesting), [Akku-Hintergrundbetrieb](https://developer.android.com/training/monitoring-device-state/doze-standby).

## Ersteinrichtung und Standardlayout

Bei einer frischen Installation ist kein BMS vorbelegt. Über **BMS auswählen → Suche starten** das eigene Bluetooth-BMS auswählen. Erst danach kann die Bereitschaft gestartet werden. Eine bereits gespeicherte oder importierte gültige BMS-Auswahl bleibt erhalten. Die App enthält keine feste Geräteadresse. Auch ein direkter Dienststart ohne gültige Auswahl wird beendet.

![Dialog zur Auswahl eines gefundenen BMS-Kandidaten](assets/screenshots/bms-auswahl.jpg)

*Der Kandidat lässt sich vor der Verbindung prüfen; die Bluetooth-Adresse ist im Screenshot geschwärzt.*

Bei einer Neuinstallation ist das dunkle Cockpit mit orangefarbenen Akzenten und deinem bereinigten Hoch- und Querformatlayout voreingestellt. Oben stehen Verbrauch der letzten 300 m und aktuelle Leistung als Rundinstrumente; die übrigen Messwerte und Bedienelemente folgen deiner Konfiguration. Grünes Startdreieck und rotes Stopquadrat steuern die Bereitschaft; das helle Pausezeichen zwischen den Instrumenten öffnet die Fahrtaktionen. **Fahrt beenden** schließt die aktuelle Aufzeichnung nach Bestätigung ab. Der Appkopf bleibt ScootPit BMS. Bestehende Installationen behalten ihr eigenes Design.

Version 1.2.1 stellt vorhandene Hochformatlayouts einmalig auf die neue Anordnung um und sichert den vorherigen Stand in den Einstellungen. Fotos, eigene Texte und zusätzliche Kacheln werden übernommen. Gespeicherte Querformatlayouts werden auf das feinere Raster umgerechnet. Das Standardlayout lässt sich jederzeit unter **Einstellungen → Cockpit bearbeiten → Standardlayout → Zurücksetzen → Speichern** übernehmen.

## Mehrere Scooter

Über den **Profilnamen im Appkopf** oder **Einstellungen → Scooter-Profile** auswählen, hinzufügen, umbenennen, kopieren und entfernen.

![Dialog zur Auswahl und Verwaltung der Scooter-Profile](assets/screenshots/scooter-profile.jpg)

*Im Profildialog wechselst du den aktiven Scooter, verwaltest Profile oder legst ein neues an.*

Beim ersten Start wird das bisherige Setup als erstes Profil übernommen. Pro Profil getrennt: BMS, Akku-Konfiguration, Gesamt-/Tages-/Tourenkilometer, Verbrauchs-/Temperaturhistorie, Routine-Texte, Statistik-Auswahl, letzte Fahrt, Fahrtenordner, Layout, Appkopf und Farben. Manuelle Profilwechsel erfordern beendete Bereitschaft. Die Automatik kann gespeicherte BMS erkennen und vor Fahrtbeginn das zugehörige Profil wählen; während einer Fahrt bleibt die Zuordnung fest. Eine Designkopie übernimmt keine BMS-Zuordnung, Kilometer, Fahrten oder Verbrauchshistorie. Entfernen eines Profils löscht keine Fahrtdateien.

Neue Fahrten liegen lokal in einem eigenen Unterordner pro Profil; GPX-Name und JSON-Metadaten tragen Scooter-Name und Profil-ID. Bereits bestehende Dateien bleiben erhalten. Die Einstellungssicherung (Formatversion 2) enthält sämtliche Profile; alte Sicherungen der Version 1 können weiterhin in das aktive Profil geladen werden. Der Einstellungen-Ordner bleibt gemeinsam, Fahrtenordner sind pro Scooter einstellbar. Nach Neuinstallation müssen Zugriffsrechte für externe Ordner neu erteilt werden.

## Separate Speicherorte

**Einstellungen → Speicherorte**: Ordner für Einstellungen und Fahrtenbuch unabhängig mit Androids Ordnerauswahl bestimmen. Einstellungen bleiben lokal als Arbeitsstand und werden automatisch im gewählten Ordner gesichert. Die Datei **Joyor-Cockpit-settings.json** und ihre Formatkennung bleiben für bestehende Sicherungen kompatibel. Eine vorhandene Datei ausdrücklich laden oder überschreiben; Import wird vor Änderungen validiert. Kacheln für beide Ausrichtungen, Farben, Verbindungsparameter und Temperatur-Lerndaten sind enthalten. Android-Ordnerberechtigungen bleiben gerätebezogen; kein ungefragter Import beim Start.

Laufende Fahrten werden lokal gepuffert. Nach Abschluss werden GPX, CSV und JSON in den bei Fahrtbeginn gewählten Fahrtenordner kopiert. Bei Fehlern bleiben lokale Dateien und Kopieraufträge erhalten; erneuter Versuch unter Speicherorte. Lokale GPX-/CSV-Dateien lassen sich über **Letzte Fahrt / Export** teilen. Ordnerwechsel gilt ab der nächsten Fahrt.

