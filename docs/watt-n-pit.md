# ScootPit mit Watt’n’Pit verwenden

[← Übersicht](../README.md) · [Bedienungsanleitung](anleitung.md)

Ab **ScootPit BMS 1.6.13** und **Watt’n’Pit 1.5.0** können die beiden offiziellen Apps auf demselben Android-Handy aktuelle BMS-Grunddaten lokal gemeinsam nutzen. Eine Verbindung zum Internet ist für diesen Austausch nicht erforderlich. Die physische Bluetooth-Übergabe muss mit dem jeweiligen Handy und BMS geprüft werden.

## Einrichten

1. Beide Apps über die vorhandene Installation aktualisieren: [ScootPit BMS](https://github.com/Tim-Gurke/ScootPit-BMS/releases/latest) · [Watt’n’Pit](https://github.com/Tim-Gurke/Watt-n-Pit/releases/latest).
2. In ScootPit unter **Einstellungen → BMS und Fahrt-Erkennung** das BMS des aktiven Scooter-Profils auswählen und eine gültige Verbindung prüfen.
3. In Watt’n’Pit den Roller mit demselben BMS auswählen. Abweichende BMS-Zuordnungen gehören nicht zu derselben Kopplung.
4. ScootPit-Bereitschaft starten und anschließend Watt’n’Pit öffnen. Watt’n’Pit kann die frischen Grunddaten über ScootPit beziehen; eine zusätzliche BMS-Verbindung ist dafür nicht erforderlich.

ScootPit erhält Vorrang für die Fahrtaufzeichnung. War Watt’n’Pit bereits direkt mit dem BMS verbunden, wird die Verbindung zunächst freigegeben und nach einer kurzen Trennpause von ScootPit übernommen. Beim Beenden der ScootPit-Bereitschaft wird die direkte Verbindung wieder freigegeben. Ein unterbrochener Prozess und die Wiederverbindung hängen zusätzlich vom Bluetooth-Verhalten des Geräts ab.

Solange Watt’n’Pit Daten des passenden BMS abfragt, wird eine aktive ScootPit-Verbindung nicht allein wegen des bisherigen Monitor-Zeitlimits beendet oder automatisch auf ein anderes Scooter-Profil gewechselt. Eine laufende Fahrt behält ihre Zuordnung.

## Was wird geteilt?

Aktuelle, gültige BMS-Basisdaten wie Spannung, Strom, Ladezustand, Kapazität, MOS-Status und die gemeldeten Temperaturen sowie die BMS-Gerätekennung zur Zuordnung. Veraltete Pakete werden nicht als frische Werte weitergereicht. Der Zugriff ist auf die offizielle Watt’n’Pit-App beschränkt.

GPS-Koordinaten, Fahrtrouten und Schlüssel gehören nicht zu dieser Schnittstelle. Abgeschlossene Fahrten werden darüber nicht übertragen. Die Kopplung umfasst keine anderen Handys und keine Drittanbieter-Apps wie Overkill. ScootPit kann weiterhin eigenständig verwendet werden.

## Am Roller prüfen

Overkill und weitere direkt mit dem BMS verbundene Apps für diesen Test beenden. Den Roller im Stand lassen und den BMS-Lastausgang freigegeben lassen.

| Reihenfolge | Erwartetes Ergebnis |
| --- | --- |
| ScootPit-Bereitschaft starten, dann Watt’n’Pit öffnen | Beide Apps erhalten aktuelle Grunddaten; ScootPit hält die BMS-Verbindung. |
| Watt’n’Pit zuerst direkt verbinden, dann ScootPit-Bereitschaft starten | Nach Freigabe und kurzer Übergangspause übernimmt ScootPit; Watt’n’Pit bezieht anschließend die Daten über ScootPit. |
| ScootPit-Bereitschaft beenden, Watt’n’Pit weiter verwenden | Watt’n’Pit kann die direkte BMS-Verbindung wieder übernehmen. |

Bei einer Rückmeldung Appversionen, Handy/Android, Startreihenfolge und beobachtete Wartezeit nennen. Ein erfolgreicher Build ersetzt diese Prüfung nicht.
