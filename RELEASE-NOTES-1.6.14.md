# ScootPit BMS 1.6.14

## Automatische BMS-Übergabe mit Watt’n’Pit 1.6.6

Die Zentrale kann beim Laden und Abstellen das Roller-BMS lesen. Zum Losfahren übernimmt ScootPit auf dem Fahrhandy automatisch, sobald du dort **Lastausgang einschalten** anforderst. Ein zusätzlicher Übergabeknopf ist nicht erforderlich.

### Einmalige Einrichtung

Watt’n’Pit **1.6.6** auf Zentrale und Fahrhandy und ScootPit **1.6.14** auf dem Fahrhandy über die vorhandenen Installationen aktualisieren. Watt’n’Pit am Fahrhandy muss als Anzeige mit der Zentrale gekoppelt sein. In beiden Apps dasselbe Roller-BMS wählen und die ScootPit-Bereitschaft mit den bisherigen Bluetooth-/Standortberechtigungen aktivieren. Auf der Zentrale ist kein ScootPit nötig. Die Installationsreihenfolge ist frei.

### Übernehmen und entsperren

- Der vorhandene Einschaltwunsch fordert die Verbindung über das lokale Watt’n’Pit an. Die Zentrale gibt ausschließlich das Roller-BMS frei; ihre Solaraufzeichnung läuft weiter.
- Der Einschaltwunsch bleibt auch vor Empfang eigener frischer BMS-Werte erreichbar. Erst nach dem Verbindungsaufbau und den bisherigen Schaltprüfungen wird der ausdrücklich angeforderte BMS-Befehl gesendet. Erfolg wird anhand des tatsächlichen BMS-Zustands bestätigt.
- Ein wartender Wunsch kann abgebrochen werden, gilt höchstens **20 Sekunden** und wird nicht gespeichert oder nach einem Dienstneustart nachgeholt.

### Unterwegs und Rückgabe

Unterwegs verbindet sich das Fahrhandy direkt mit dem BMS; Zentrale, Heimnetz und Internet sind dafür nicht erforderlich. Auch bei Fahrtpause oder aktivierter Wegfahrsperre bleibt es zuständig. Aus- und erneutes Einschalten benötigen keine Freigabe vom entfernten Hauptgerät.

Die reguläre Rückgabe erfolgt erst bei stabil erreichbarer Zentrale, beendeter Fahrt und frisch bestätigtem ausgeschaltetem Lastausgang. Die automatische Fahrtende-Erkennung kann eine längere Pause abwarten; eine reine Fahrtpause gibt das BMS nicht zurück. Eine bereits gesperrte Maschine muss nicht erneut gesperrt werden. Laden ist keine Voraussetzung.

ScootPit trennt vor der Übernahme durch die Zentrale und hält eigene Verbindungsversuche zurück. Frische BMS-Werte an der Zentrale bestätigen die Rückgabe. Keine Übergabe schaltet den Lastausgang selbständig.

### Fehler und Grenzen

Veraltete Anfragen früherer Dienstprozesse und verspätete Übergabeaktionen werden abgewiesen. Verlorene Reservierungen der Zentrale laufen begrenzt ab; ein Wiederanlauf ändert den BMS-Lastausgang nicht. Die BMS-Zuständigkeit erscheint beim Lastausgang, der Verbindungsverlauf unter Watt’n’Pit → Zusatzspeicher.

Ohne gekoppeltes oder erreichbares Hauptgerät bleibt die direkte BMS-Verbindung nutzbar. Ist die Zentrale am selben Ort noch per Bluetooth verbunden, benötigt ihre Freigabe das lokale Netz. Fahrtrouten und GPS-Positionen werden über diese Schnittstelle nicht weitergegeben.

Bestehende Cockpitlayouts, Fahrten, Profile und Einstellungen bleiben erhalten. Die bisherige lokale Kopplung auf demselben Handy bleibt unterstützt.

Automatische Logik-, Android-Build-, Signatur- und Emulatorprüfungen prüfen die Software. Echte BMS-Verbindung, Freigabe und Rückgabe zwischen physischen Handys müssen am Roller praktisch geprüft werden.
