# BMS und Kompatibilität

[← Übersicht](../README.md)

## Roller und BMS-Kompatibilität

ScootPit BMS wurde für den **Joyor T6e Pro mit JBD/Jiabaida-Bluetooth-BMS (SP14S004)** entwickelt. Entscheidend ist das tatsächlich eingebaute BMS samt Firmware und Bluetooth-Zugang; ein gleicher Markenname, „Smart BMS“ oder eine Bluetooth-Verbindung allein reichen nicht als Kompatibilitätsnachweis. Vollständige Hardwaretests von ScootPit BMS sind weiterhin erforderlich.

| Roller / Akku | Beleg und Einordnung | ScootPit BMS-Status |
| --- | --- | --- |
| Joyor T6e Pro, Projektfahrzeug | Entwicklungsbasis dieses Projekts: JBD SP14S004. Keine pauschale Zusage für jede Bau-/Akkuversion der T6-Serie. | Zielgerät; reale Messung, Hintergrundbetrieb und Lastausgang am konkreten Roller prüfen. |
| IO HAWK Legacy 2.0 | Das [Herstellerhandbuch, S. 19](https://www.iohawk.de/wp-content/uploads/2023/04/Handbuch_Legacy2.0.pdf) nennt Xiaoxiang BMS per Bluetooth und eine Hardware-PIN für die Sperrfunktion. Die genaue Platinenbezeichnung ist dort nicht belegt. | Kandidat für einen Kompatibilitätstest, noch nicht mit ScootPit BMS geprüft; PIN-/Sperrverfahren können abweichen. |
| RoadRunner RX7 | Das [vom Hersteller verlinkte Handbuch, S. 11–12](https://cdn.shopify.com/s/files/1/0276/7126/5364/files/RoadRunner_Scooters_RX7_owners_manual_digital_8.3.26.pdf?v=1785753584) beschreibt XiaoXiang Electric Smart BMS samt Registrierung/QR-Zuordnung. Das belegt weder SP14S004 noch dessen identischen Zugang. | Kandidat, noch nicht geprüft; Zugang und BLE-Protokoll vor Nutzung verifizieren. |
| Andere Roller mit nachgerüstetem JBD/Xiaoxiang-BMS | Der [BMS-Hersteller](https://jbdbms.com/products/sp14s004-2093589136) führt SP14S004 für 7–14S-Akkus mit integriertem Bluetooth und E-Scooter-Anwendung auf. | Technisch möglicher Einsatz, keine Freigabe einer ganzen Rollermodellreihe. |
| Weitere JBD-Varianten, z. B. SP04S020, SP22S003B und UP16015 | Die [Community-Komponente für ESPHome](https://github.com/syssi/esphome-jbd-bms) führt diese und weitere JBD-Modelle als erprobt auf. Sie belegt weder den Einbau in einen bestimmten Roller noch, dass dessen BMS denselben BLE-Zugang verwendet. | Protokollkandidaten; vor Aufnahme müssen BLE-Dienst, gültige Antwort und Messwerte am konkreten Gerät geprüft werden. |

Bei anderen Joyor-Modellen, Xiaomi/Ninebot, Vsett, Kaabo usw. wird **keine Serienkompatibilität behauptet**.

### Voraussetzungen für weitere Geräte

- Aktuell implementiert: unverschlüsselte JBD-Basic-Info-Abfrage `0x03` über BLE-Service `FF00`, Notify `FF01`, Write `FF02` (Bluetooth-Basis-UUID). Ein vollständiger gültiger Antwortframe ist erforderlich.
- Keine Implementierung einer BMS-PIN-Anmeldung, QR-Konto-Zuordnung oder herstellerspezifischer Authentifizierung. Geräte, die diese beim Lesen oder Schreiben verlangen, sind damit derzeit nicht unterstützt.
- Unter **Einstellungen → BMS** das konkrete Gerät auswählen. Akku-Kapazität, Nennspannung, Verbrauchsstartwert und Stromschwellen an den Roller anpassen; die Vorgaben 48 V/26 Ah sind auf das Projektfahrzeug bezogen.
- Messwerte und Fahrt-Erkennung getrennt von der Steuerfunktion prüfen. JBD-V4-`E1` zum Lastausgang ist nicht für jedes JBD-Modell garantiert; nur mit bestätigtem Verhalten am stehenden Roller verwenden. Keine Schutzparameter oder PINs ändern.
- Sensoranzahl, Zuordnung und Verbindungsverhalten können abweichen. Die ersten zwei Temperatursensoren sind Standardkacheln; weitere gelesene Sensoren können als optionale Kacheln ergänzt werden.
- Bereitschaft benötigt weiterhin ein auslesbares BMS: Es gibt derzeit keinen eigenständigen Startmodus für Roller ohne kompatibles BMS.

Besitzer anderer Roller können sich gern melden und ihre Kombination für einen Kompatibilitätstest vorschlagen. Für eine Prüfung oder einen bestätigten Eintrag bitte Roller-/Akkuversion, BMS-Typ und Firmware, vorhandene BLE-UUIDs sowie Ergebnisse für Lesen, Fahrtstart/-ende und gegebenenfalls Lastausgang über [GitHub Issues](https://github.com/Tim-Gurke/ScootPit-BMS/issues) mitteilen. Kandidaten sind erst nach erfolgreichem Test am konkreten Gerät bestätigt. Bitte keine MAC-Adresse, PIN oder Kontodaten veröffentlichen. [ESPHome JBD-BMS](https://github.com/syssi/esphome-jbd-bms) dokumentiert zahlreiche JBD-Varianten und Protokollunterschiede, bestätigt aber nicht deren Funktion in ScootPit BMS.

Recherchestand: 12.10.2026.

## BMS-Auswahl und optionale Daten

Die Geräteauswahl ist ein eigener großer Dialog mit scrollbarer Liste. BMS-Kandidaten anhand Name oder angekündigtem JBD-Dienst erscheinen zuerst; **Alle BLE-Geräte anzeigen** macht auch unbekannte Geräte zugänglich. Signalstärke und Gerätename werden aktualisiert. Suche starten/stoppen und manuelle Adresseingabe sind verfügbar. Eine neue Auswahl wird erst nach passendem FF00/FF01/FF02-Dienst und gültiger JBD-03-Antwort gespeichert. Während der Prüfung darf keine andere Verbindung zum BMS aktiv sein. Nicht antwortende Geräte bleiben unbestätigt; ein fehlgeschlagener Versuch beweist keine grundsätzliche Inkompatibilität.

Unter **Cockpit bearbeiten → + Kachel → BMS-Daten (optional)** werden erfolgreiche Datenabfragen dieses Profils angeboten. Standard-JBD-Abfragen: 03 (Basiswerte), 04 (Zellspannungen), 05 (Gerätekennung), AA (Ereigniszähler). Kapazität in Ah, Zyklen, Firmware, Herstellungsdatum, Zellanzahl, Balancier-/Schutzstatus, MOS-Freigaben, einzelne Zellgruppen und weitere Temperatursensoren. Aus Zellspannungen berechnet: Minimum, Maximum, Mittelwert, Differenz und Nummern der Extremwerte. Ereigniszähler werden nur bei einer gültigen 24-Byte-AA-Antwort angeboten. Firmwarevarianten können weniger liefern. Hersteller/Barcode und die vollständige Schutzparameter-Konfiguration werden nicht automatisch zugesagt; sie sind nicht vollständig durch die genannten Standardabfragen abgedeckt.

Gültige Nullwerte bleiben sichtbar. Erkannte Datenarten werden pro Profil gespeichert; vorhandene Kacheln bleiben bei Verbindungsverlust erhalten. Für Zusatzwerte gilt eine eigene Aktualitätsprüfung. Konfigurationsschreiben, Schutzparameteränderungen und Zähler-Reset werden nicht ergänzt. Bestehende Wegfahrsperren-Bestätigung und Stillstandsprüfung bleiben bestehen.

Beim Joyor des Projektbesitzers sind **26 Ah** korrekt. Dies ist der editierbare Ersatzwert; gültige BMS-Kapazitäten haben Vorrang. Temp1/Temp2 bleiben neutral, mit editierbaren Profilbeschriftungen. Temp1 als Platinenfühler und Temp2 als Akkufühler ist aufgrund des beobachteten Temperaturverlaufs bisher eine Vermutung.

Protokollreferenz: [ESPHome JBD-BMS Decoder](https://github.com/syssi/esphome-jbd-bms/blob/main/components/jbd_bms/jbd_bms.cpp). Screenshots werden später neu ergänzt.

## BMS-Verbindungsprüfung

Nach dem Aktivieren des Antwortkanals wartet die Auswahl 800 ms und wiederholt die reine Basisdaten-Leseabfrage bei fehlender Antwort bis zu achtmal im Abstand von 2 Sekunden. Neue Geräte werden weiterhin erst mit gültigen Basisdaten gespeichert. Bluetooth-Schreibfehler und Zeitüberschreitungen beim Verbindungsaufbau, bei der Dienstsuche oder der Datenabfrage werden getrennt angezeigt. Vor der Prüfung andere Bluetooth-Verbindungen zum BMS trennen und das Handy nahe an den Roller halten. Ein Emulator kann den tatsächlichen BLE-Antwortablauf des Rollers nicht bestätigen.

## BMS-Lastausgang / Wegfahrsperre

Nur Zustandsfarben: **grün** = Entladung freigegeben, **rot** = Entladung gesperrt, **grau** = kein bestätigter frischer Zustand / Schaltung ausstehend. Eine manuelle Sperre ist dabei nicht von einer BMS-Schutzabschaltung unterscheidbar. Niemals automatisches Schalten beim App-Start oder Verbindungsaufbau.

Beide Schaltrichtungen benötigen frische BMS-Daten und keine gemeldete GPS-Bewegung. Eine laufende Fahrt muss vor dem Sperren manuell pausiert sein; die Pause kann bis zu 12 Stunden dauern und bleibt dieselbe Fahrt. Entsperren ist auch ohne gelernte Standstrom-Referenz möglich. Zum Sperren lernt die App nach einer erkannten Fahrt für jedes Scooter-Profil einen stabilen Gesamtstrom bei eingeschaltetem Roller im GPS-Stillstand. Das BMS misst den Packstrom; der Verbrauch des Displays wird nicht separat erfasst. Gesperrt wird erst, wenn der aktuelle Strom mindestens 5 Sekunden sicher unter der Referenz liegt. Ist die Referenz noch unbekannt oder reicht die Auflösung nicht zum sicheren Vergleich, bleibt der Lastausgang an. Nur am stehenden Roller benutzen; ohne GPS-Fix kann die App Stillstand nicht selbst garantieren. JBD-V4-Befehl E1 mit Prüfsumme, Fehlerauswertung und anschließendem FET-Readback; Timeout nach 10 s ohne automatischen erneuten Versand. Kein Factory-Mode oder Parameter-Schreiben.

E1 betrifft Lade- und Entladesperre gemeinsam. Bei ausgeschaltetem Lade-MOS werden Befehle abgelehnt, damit eine vorhandene Ladesperre erhalten bleibt. Steuerfunktion, BMS-Neustartverhalten und Bluetooth-Erreichbarkeit müssen am konkreten BMS getestet werden. Die App entsperrt niemals automatisch.

