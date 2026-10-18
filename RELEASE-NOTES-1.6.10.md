# ScootPit BMS 1.6.10

<img src="https://raw.githubusercontent.com/Tim-Gurke/ScootPit-BMS/v1.6.10/docs/assets/screenshots/verbindungsdiagnose-1.6.10.png" width="360" alt="Neue Verbindungsdiagnose mit gekennzeichnetem Beispielablauf">

*Echte Appaufnahme aus dem Android-Emulator; der Ablauf ist ein gekennzeichnetes Beispiel.*

- **Korrigierte Höhenwerte:** Neue Fahrten verwenden die Höhe über dem mittleren Meeresspiegel (MSL). Die standortabhängige Umrechnung aus der GPS-Ellipsoid-Höhe erfolgt vollständig auf dem Handy und funktioniert auch ohne Internet ab Android 8.
- **Einheitlicher Höhenbezug:** Cockpit, neues Höhendiagramm, GPX und CSV nutzen denselben korrigierten Wert. CSV enthält zusätzlich den ursprünglichen GPS-Höhenwert und den Höhenbezug. Beim erneuten Import bleibt der Bezug erhalten; Werte werden nicht doppelt korrigiert. Fehlende Höhen bleiben leer.
- **CSV-Import verbessert:** Spalten für Höhe, Geschwindigkeit, Leistung und Temperaturen werden auch mit den von ScootPit verwendeten Unterstrichen im Spaltennamen zuverlässig erkannt.
- **Bestehende Fahrten:** Originaldateien bleiben erhalten. Ältere eigene Fahrten tragen im Höhendiagramm einen Hinweis auf den WGS84-Ellipsoid-Bezug; bei importierten Dateien ohne entsprechende Angaben bleibt der Bezug als unbekannt gekennzeichnet. Die MSL-Schätzung ist kein amtlicher NHN-Höhenanschluss.
- **Bluetooth-Diagnose:** **Einstellungen → Verbindungsdiagnose** zeigt Suchpausen, Empfang, Verbindungsphasen und die Dauer bis zur ersten gültigen BMS-Antwort. Der Bericht lässt sich aktualisieren, kopieren oder über die Android-Appauswahl teilen. Er enthält keine Geräteadresse, Profilnamen oder Fahrtroute und wird nicht automatisch versendet.
- **Verbindungsfehler klarer behandeln:** Ein von Android abgelehnter Verbindungs- oder Dienstsuchstart wird erkannt und erneut versucht. Empfangsschwellen und individuelle Erkennungseinstellungen bleiben erhalten.
- **Anleitung und Gerätetests:** [Höhenwerte und Fahrtenbuch](https://github.com/Tim-Gurke/ScootPit-BMS/blob/v1.6.10/docs/fahrtenbuch.md), [Verbindungsdiagnose](https://github.com/Tim-Gurke/ScootPit-BMS/blob/v1.6.10/docs/verbindungsdiagnose.md) und [bisherige Gerätemeldungen](https://github.com/Tim-Gurke/ScootPit-BMS/blob/v1.6.10/docs/geraetetests.md) sind dokumentiert.

Die signierte APK lässt sich über die bestehende Installation installieren. Profile, Kilometerzähler, persönliche Cockpit-Anordnungen und Fahrtdateien bleiben erhalten.
