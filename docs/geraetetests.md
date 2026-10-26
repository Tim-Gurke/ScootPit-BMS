# Geräte und bisherige Rückmeldungen

[← Übersicht](../README.md) · [BMS-Kompatibilität](bms.md)

**Stand: 26. Oktober 2026.** Die Entwicklungsbasis ist der Joyor T6E Pro mit JBD SP14S004. Ein erfolgreich gestartetes Programm bestätigt zunächst Installation und Start. Für den Einsatz müssen außerdem BMS-Verbindung, GPS-Aufzeichnung und Betrieb bei gesperrtem Bildschirm in der jeweiligen Gerätekombination geprüft werden.

## 1.6.13: nächste Praxistests

Die Version übernimmt die lokale Watt’n’Pit-Kopplung und die Korrektur für Verbrauch ab Reset aus den Testständen. Pause, zeitweiliger BMS-Ausfall, Fortsetzen und die Bluetooth-Übergabe zwischen beiden Apps sind am realen Roller noch zu prüfen. [Testabläufe zur Kopplung](watt-n-pit.md#am-roller-prüfen) · [Verbrauch bei Fahrtpausen](cockpit.md#durchschnittswerte-bei-fahrtpausen-ab-1613).

Zur BMS-Sperre liegt die Rückmeldung vor, dass das Sperren nach der lastarmen Wartefrist funktioniert, aber auch bei eingeschaltetem Roller möglich ist. Ein beobachteter Nullstrom im Stand liefert dafür kein verlässliches Ein-/Aus-Signal. Die Schaltlogik wurde für 1.6.13 nicht verändert; vor dem Sperren den Roller selbst ausschalten. Dieser Befund ersetzt keine allgemeine Prüfung weiterer BMS- und Controllerkombinationen.

## Öffentliche Praxismeldungen

Quelle: [Forenthread „Joyor T6e Pro Tracking & Cockpit“, Beiträge 12 und 14](https://www.escooter-treff.de/threads/joyor-t6e-pro-tracking-cockpit.28049/). Die Modell- und Androidangaben stammen aus den Berichten und wurden nicht unabhängig geprüft.

| Gerät laut Bericht | Android laut Bericht | Gemeldeter Umfang | Noch offen |
| --- | --- | --- | --- |
| Oukitel WP12 | „8 mit Pseudo-Update auf 11“; tatsächlicher Systemstand unklar | ScootPit 1.6.9 installiert und zunächst unauffällig gestartet | Genaue Androidversion, Fahrtaufzeichnung und Hintergrundbetrieb |
| Poco M6 | 16 | ScootPit 1.6.9 installiert und zunächst unauffällig gestartet | Verbindung und vollständige Fahrt mit gesperrtem Bildschirm |
| Redmi Note 15 | 16 | ScootPit 1.6.9 installiert und zunächst unauffällig gestartet | Verbindung und vollständige Fahrt mit gesperrtem Bildschirm |
| Xiaomi „T13 Pro“ | 16 | Allgemeiner Betrieb unauffällig; verzögerter BMS-Verbindungsaufbau und auffälliger Höhenversatz gemeldet | Verbindung mit Diagnosebericht eingrenzen; korrigierte Höhe im Alltag nachprüfen |
| Lenny 5 | 6 | Als zu alt gemeldet | Unterhalb der Mindestversion Android 8 |

## Automatische Prüfungen

Der Android-Testlauf prüft die Offline-Höhenumrechnung unter Android **8, 13 und 15**, einschließlich fehlender Höhenwerte und erneutem Import von GPX und CSV. Unter Android 13/15 kommen Cockpit-, Menü-, Layout- und Hintergrunddienstprüfungen im Emulator hinzu. Die Ergebnisse stehen in den [GitHub-Actions-Läufen](https://github.com/Tim-Gurke/ScootPit-BMS/actions).

Für **1.6.10** sind die automatischen Prüfungen erfolgreich abgeschlossen ([Prüflauf](https://github.com/Tim-Gurke/ScootPit-BMS/actions/runs/39121348866)):

| Emuliertes Android | Erfolgreich geprüft |
| --- | --- |
| 8.0 / API 26 | Höhenumrechnung ohne Netz, fehlende Höhen und CSV-/GPX-Export mit erneutem Import |
| 13 / API 33 | Dieselben Höhentests sowie Cockpit, Diagnosemenü, Einstellungen, Layouts und Hintergrunddienst |
| 15 / API 35 | Dieselben Höhentests sowie Cockpit, Diagnosemenü, Einstellungen, Layouts und Hintergrunddienst |

Eine echte BMS-Verbindung und GPS-Empfang am fahrenden Scooter lassen sich damit noch nicht bestätigen.

**Zusätzliche Prüfung in 1.6.11:** [Prüflauf zur Sperrfunktion](https://github.com/Tim-Gurke/ScootPit-BMS/actions/runs/39271313706). Der Java-Regressionstest stellt eine durch Reststrom unbrauchbar gewordene Standstrom-Referenz nach und prüft die Korrektur sowie das erneute Lernen alter unbrauchbarer Werte. Unter Android 13/15 wird ein Sperrversuch im echten Bestätigungsdialog ausgelöst und die Ablehnung durch den Dienst ohne frische BMS-Verbindung sichtbar geprüft. Weitere Fälle sind fehlende Referenz, fremde/erneute Statusmeldungen und das Speichern einer ausstehenden Anfrage für einen Orientierungswechsel. Das ist keine Bestätigung eines realen MOS-Schaltvorgangs am Fahrzeug.

## Was hilft bei einer Rückmeldung?

**Prüfung der 1.6.12:** [Android-Prüflauf ohne Referenzpflicht](https://github.com/Tim-Gurke/ScootPit-BMS/actions/runs/39278246436). Der Java-Test prüft die Freigabe ohne gelernte Referenz oder vorherige Fahrt nach fünf Sekunden geringer Stromaufnahme, kleine Restströme, Messauflösung und die verbleibenden Sperrbedingungen. Unter Android 13/15 werden der neue Bestätigungsdialog, die verständliche Warteerklärung und das Cockpit mit alten leeren Referenzwerten geprüft. Eine Prüfung des realen Schaltverhaltens des Fahrzeugcontrollers ist darin nicht enthalten.

- Appversion, genaues Handymodell und Androidversion aus den Systemeinstellungen
- Scooter- und BMS-Modell, soweit bekannt
- Ergebnis von Verbindungsaufbau, GPS-Route und Aufzeichnung bei gesperrtem Bildschirm
- Bei Verzögerungen der [Verbindungsdiagnosebericht](verbindungsdiagnose.md) und eine ungefähre Wartezeit

Persönliche Geräteadressen oder eine private Fahrtroute sind dafür nicht nötig.
