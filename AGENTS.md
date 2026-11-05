# Entwicklung und Veröffentlichung

## Gemeinsamer Standard für alle Pit-Apps

Gilt für Watt’n’Pit, WohnPit, ScootPit BMS und AirPit sowie zukünftige Apps und Repositories von Tim. Vereinbart am 05.11.2026. Bei den nächsten regulären Updates jede App anhand dieser Vorgaben prüfen, bereinigen und vereinheitlichen. Die folgenden Punkte sind Anforderungen für die Umsetzung, keine Behauptung, dass alle Apps sie bereits erfüllen.

- Zusammengehörige Messwerte erhalten zentral verwaltete Farben. Eine Änderung gilt in Kachelauswahl, Zahlen, Rundinstrumenten, Diagrammen und Detailansichten. Farbcodes als #RRGGBB anzeigen, eingeben und kopieren lassen; Einstellungen mit sichern. Bewusste eigene Kachelfarben klar kenntlich machen. In Watt’n’Pit bleiben Solar orange, Batterie grün und berechnete Akkubilanz hellblau; Wasser und Gas erhalten unterscheidbare eigene Farben. Benutzerdefinierte Farben unverändert übernehmen.
- Bedienung, Einstellungen, App-Kopf mit Versionsnummer, Hell-/Dunkelansicht, große Bedienelemente und Copyright-Vermerk appübergreifend konsistent gestalten, soweit fachlich passend. Aktive, pausierte und nicht verfügbare Funktionen klar beschriften; nicht verfügbare Aktionen erklären und deaktivieren. Bedeutung nie ausschließlich durch Farbe vermitteln.
- Datenabrufe nachvollziehbar machen: Zeitpunkt allein belegt keine Vollständigkeit. Bei rückwirkenden Abrufen den tatsächlich gespeicherten Zeitraum, Datenort/Quelle, vorhandene und fehlende Tage sowie laufenden Abruf und Fehler zeigen. Vorhersagen gelten nicht als geladene historische Wetterdaten. Unbekannte Werte nicht als Null oder erfolgreich geladen darstellen.
- Vorhandene Messdaten, Profile, Kopplungen und pro Gerät/Ausrichtung gespeicherte Layouts erhalten. Eine ausdrücklich gewünschte Layoutübernahme höchstens einmal ausführen; spätere eigene Änderungen erhalten. Umbauten mit Datenmigration und kompatiblen Versionsständen dokumentieren.
- Jede Veröffentlichung enthält die installierbare APK mit bestehender Updatesignatur, aktuelle sichtbare Screenshots im README, ausführliche Installations-, Einrichtungs-, Bedien- und Update-Anleitungen sowie eine vollständige Versionshistorie. Passende Prüfungen ausführen und deren Grenzen nennen. Keine realen Hardwaretests behaupten, wenn nur Softwareprüfungen erfolgt sind.
- Nach erfolgreicher Veröffentlichung nur die drei neuesten Releases je Repository veröffentlicht lassen. Ältere Release-Veröffentlichungen entfernen, Quellhistorie, Tags und Änderungsdokumentation erhalten. Private Schlüssel, Zugangsdaten und persönliche Mess-/Standortdaten nicht veröffentlichen.

## Planung: WohnPit und Watt’n’Pit

Eine mögliche spätere Zusammenführung prüfen; sie ist noch nicht als Umsetzung beschlossen. Bis dahin bleibt WohnPit die schlanke Datenbasis mit Aufzeichnung, Wetterabruf und Rohdatenarchiv. Watt’n’Pit übernimmt Bedienung, Berechnung, Lernen und Anzeige. Gemeinsame Anlagenparameter müssen von den Anzeigehandys aus steuerbar bleiben.

Vor einer Zusammenführung einen konkreten Vorschlag dokumentieren: gemeinsame Codebasis oder gemeinsame APK mit Rollen „Zentrale“, „Anzeige“ und gegebenenfalls „Kombiniert“; Betrieb auf dem schwächeren G5 mit gesperrtem Display; separate Bedienhandys; Daten- und Einstellungsübernahme; Bluetooth-/BMS-Übergabe und bestehende Verbindungen; Updatesignaturen und Android-Paketkennungen; Upgrade-/Rückweg und Versionskompatibilität. Erst nach ausdrücklicher Entscheidung die Apps zusammenführen. Bestehende Installationen müssen bis zum geprüften Übergang weiter funktionieren.
