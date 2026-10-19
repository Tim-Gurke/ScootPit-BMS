# ScootPit BMS 1.6.12

<img src="https://raw.githubusercontent.com/Tim-Gurke/ScootPit-BMS/v1.6.12/docs/assets/screenshots/bms-sperren-1.6.12.png" width="360" alt="Neue Sperrbestätigung mit direkter Stromprüfung ohne Lernreferenz">

*Echte Appaufnahme unter Android 13 im Emulator, mit einem Testablauf ohne reales BMS.*

- **Wegfahrsperre ohne Anlernen:** Die Pflicht zur gelernten Standstrom-Referenz ist vollständig entfernt. Eine fehlende Referenz, ein alter Wert oder eine nicht durchgeführte Lernfahrt blockiert das Sperren nicht mehr.
- **Direkte Stromprüfung:** Bei frischen BMS-Daten, pausierter/beendeter Fahrt und keiner gemeldeten Bewegung prüft die App mindestens fünf Sekunden geringe Stromaufnahme unter 0,15 A. Die Messauflösung wird berücksichtigt. Höhere Last, Messlücken oder ein Verbindungsabbruch setzen die Wartefrist zurück.
- **Aktueller Hinweis im Cockpit:** Unter dem Schalter steht der momentane Sperrgrund. Nach einem abgelehnten Versuch zeigt ein eigener Dialog den Grund. Eine Aufforderung zum Anlernen erscheint nicht mehr.
- **BMS-Bestätigung bleibt erforderlich:** Erfolg wird erst nach BMS-Antwort und passendem ausgelesenen MOS-Zustand gemeldet. Lade-MOS-Schutz und die Bestätigung vor dem Schaltbefehl bleiben erhalten.

**Bedienung:** Roller anhalten, eine offene Fahrt pausieren oder beenden, Roller ausschalten und die Bereitschaft anlassen. Fünf Sekunden geringe Stromaufnahme abwarten, den BMS-Schalter antippen und „Sperren“ bestätigen. Eine vorherige Fahrt oder GPS-Empfang zum Lernen sind nicht erforderlich.

[Anleitung](https://github.com/Tim-Gurke/ScootPit-BMS/blob/v1.6.12/docs/anleitung.md#11-bms-lastausgang-und-optionale-wegfahrsperre)

Die signierte APK lässt sich über die vorhandene App installieren. Profile, Zähler, Cockpit-Anordnungen und Fahrten bleiben erhalten.
