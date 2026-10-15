# ScootPit BMS 1.6.6

![ScootPit BMS zeigt Bereitschaft bei ausgeschaltetem BMS-Lastausgang](https://raw.githubusercontent.com/Tim-Gurke/ScootPit-BMS/main/docs/assets/screenshots/cockpit-lastausgang-aus-1.6.6.jpg)

*Ausgangszustand aus dem bereitgestellten Screenshot; 1.6.6 ergänzt den Hinweis auf den ausgeschalteten Lastausgang in der Statuszeile.*

- Eine Fahrt lässt sich manuell bis zu 12 Stunden pausieren und danach als dieselbe Fahrt fortsetzen. Während der Pause kann die Wegfahrsperre eingeschaltet werden.
- Die App lernt pro Scooter im Hintergrund den stabilen Gesamtstrom des eingeschalteten Rollers im Stillstand (Display an). Das ist ein Standstrom-Referenzwert, keine isolierte Messung des Displays. Abschalten des Lastausgangs bleibt gesperrt, solange der gemessene Strom nicht sicher unter dem gelernten Wert liegt. Fehlt ein belastbarer Messwert oder reicht die BMS-Messauflösung dafür nicht aus, bleibt Abschalten gesperrt.
- Ist der BMS-Lastausgang aus, zeigt die Bereitschaftszeile den Hinweis „BMS-Lastausgang aus – bitte einschalten“.
- Das Handydisplay bleibt bei aktiver Bereitschaft eingeschaltet, solange das Cockpit im Vordergrund angezeigt wird.
- Die große Verbrauchsskala startet wie in der Einstellungsdatei bei **40**. Das Update übernimmt den neuen Wert für unveränderte Standardlayouts; persönliche Skalenwerte bleiben erhalten.

Die signierte APK kann über die bestehende Installation installiert werden. Scooter-Profile, Zähler und gespeicherte Fahrten bleiben erhalten.
