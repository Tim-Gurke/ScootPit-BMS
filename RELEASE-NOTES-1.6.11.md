# ScootPit BMS 1.6.11

<img src="https://raw.githubusercontent.com/Tim-Gurke/ScootPit-BMS/v1.6.11/docs/assets/screenshots/bms-sperrhinweis-1.6.11.png" width="360" alt="Deutlich sichtbarer Hinweis nach einem abgelehnten Sperrversuch">

*Echte Appaufnahme unter Android 15 im Emulator. Der fehlende Referenzwert ist ein Testbeispiel; es ist kein reales BMS verbunden.*

- **Standstrom-Erkennung korrigiert:** Winzige Restströme an der Messauflösungsgrenze überschreiben keine brauchbare Standstrom-Referenz mehr. Bisher konnte dadurch selbst bei null Strom kein Sperrbefehl freigegeben werden.
- **Unbrauchbare Altwerte erkennen:** Eine gespeicherte Referenz, bei der selbst null Ampere den Abschaltvergleich nicht bestehen könnten, wird neu ermittelt. Gültige Referenzen bleiben erhalten. Falls die Referenz fehlt: Nach einer Fahrt den eingeschalteten Roller mit GPS-Empfang etwa zwölf Sekunden ruhig stehen lassen, bis die Referenz gelernt ist; anschließend pausieren/beenden, Roller ausschalten und mindestens fünf Sekunden warten.
- **Sperren mit sichtbarer Rückmeldung:** Nach „Sperren“ oder „Freigeben“ erscheinen Ablehnungsgrund, Übertragungsfehler oder fehlende Bestätigung in einem eigenen Dialog. Die Rückmeldung gehört zu genau diesem Versuch. Erfolg wird weiterhin erst nach BMS-Bestätigung und passendem ausgelesenen MOS-Zustand gemeldet.
- **Schaltablauf nachvollziehen:** Die Verbindungsdiagnose protokolliert zusätzlich die Schaltphasen und feste Ablehnungsgründe, ohne Geräteadresse, Profilnamen, Route oder elektrische Fahrtmesswerte.

[Anleitung zur Sperrfunktion](https://github.com/Tim-Gurke/ScootPit-BMS/blob/v1.6.11/docs/anleitung.md#11-bms-lastausgang-und-optionale-wegfahrsperre)

Die signierte APK lässt sich über die vorhandene App installieren. Profile, Zähler, Cockpit-Anordnungen und Fahrtdateien bleiben erhalten. Stillstands-, Strom-, Frische- und BMS-Bestätigungsprüfungen bleiben bestehen; es gibt keine erzwungene Abschaltung oder Änderung von BMS-Schutzparametern.
