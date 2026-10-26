# ScootPit BMS 1.6.13-test.1

Begleitendes Testupdate für Watt’n’Pit 1.5.0-test.2. Keine öffentliche Freigabe.

ScootPit stellt aktuelle, geprüfte BMS-Grunddaten über eine lokale, auf die offizielle Watt’n’Pit-App beschränkte Android-Schnittstelle bereit. Watt’n’Pit muss ScootPit dafür nicht mehr vom BMS trennen. Es werden keine GPS-Daten, Fahrspuren oder Schlüssel übertragen.

Ein gemeinsamer Verbindungsmechanismus verhindert absichtliche konkurrierende BMS-Verbindungsversuche. ScootPit erhält Vorrang, wartet bei aktiver Watt’n’Pit-Direktverbindung auf deren Freigabe und eine kurze Trennpause. Bei Beenden der Bereitschaft wird die Verbindung wieder freigegeben. Bei Prozessausfall läuft eine Fremdbelegung nach zehn Sekunden ab; erneute BLE-Verbindungsversuche bleiben vom tatsächlichen Gerät abhängig.

Wenn Watt’n’Pit passende BMS-Daten benötigt, wird die Verbindung nicht allein wegen des bisherigen 90-Sekunden-Monitorlimits geschlossen oder auf ein anderes Rollerprofil umgeschaltet. Fahrterkennung, Aufzeichnung und Bedienelemente behalten ihre bisherigen Funktionen.

Beide Apps auf demselben Handy aktualisieren und dasselbe BMS auswählen. Unterschiedliche Handys und Overkill sind nicht Teil dieser Kopplung. Der Austausch abgeschlossener Fahrten ist noch nicht enthalten. Die physische Bluetooth-Übergabe muss am Roller geprüft werden.
